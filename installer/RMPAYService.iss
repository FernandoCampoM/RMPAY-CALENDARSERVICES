; ============================================================================
;  Instalador de RMPAY Service (Inno Setup 6).
;  - Pide la configuración (SQL Server, Retail Manager, Shopify, puertos) y la
;    escribe en {app}\config\application.properties, que Spring Boot carga por
;    encima del application.properties del jar.
;  - Registra la aplicación como servicio de Windows con WinSW (RMPAYService.exe).
;  - Crea en el escritorio un acceso directo al panel de administración local.
;  Se compila con installer\build.ps1, que prepara {#StageDir} (jar, runtime de
;  Java hecho con jlink y WinSW). Al reinstalar sobre una versión anterior, las
;  páginas aparecen con la configuración actual.
; ============================================================================

#ifndef AppVersion
  #define AppVersion "1.0.0"
#endif
#ifndef StageDir
  #define StageDir "build\stage"
#endif

#define AppName "RMPAY Service"

[Setup]
AppId={{8F3C2A41-6B7E-4D2A-9C1F-3E5A7B9D2C64}
AppName={#AppName}
AppVersion={#AppVersion}
AppVerName={#AppName} {#AppVersion}
AppPublisher=Retail Manager
VersionInfoVersion={#AppVersion}
DefaultDirName={autopf}\RMPAY Service
DisableDirPage=auto
DefaultGroupName={#AppName}
DisableProgramGroupPage=yes
OutputBaseFilename=RMPAYService-Setup-{#AppVersion}
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
; Windows 10 / Server 2016 o superior (requisito del runtime de Java incluido)
MinVersion=10.0
UninstallDisplayName={#AppName}
UninstallDisplayIcon={app}\RMPAYService.exe
CloseApplications=no
SetupLogging=yes

[Languages]
Name: "es"; MessagesFile: "compiler:Languages\Spanish.isl"

[Files]
Source: "{#StageDir}\runtime\*"; DestDir: "{app}\runtime"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#StageDir}\app\RMPAYservice.jar"; DestDir: "{app}\app"; Flags: ignoreversion
Source: "{#StageDir}\RMPAYService.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#StageDir}\RMPAYService.xml"; DestDir: "{app}"; Flags: ignoreversion

[Dirs]
Name: "{app}\config"
Name: "{app}\logs"

[INI]
Filename: "{commondesktop}\Panel RMPAY.url"; Section: "InternetShortcut"; Key: "URL"; String: "{code:GetPanelUrl}"
Filename: "{group}\Panel de administración RMPAY.url"; Section: "InternetShortcut"; Key: "URL"; String: "{code:GetPanelUrl}"

[Icons]
Name: "{group}\Registros del servicio"; Filename: "{app}\logs"
Name: "{group}\Desinstalar {#AppName}"; Filename: "{uninstallexe}"

[Run]
Filename: "{code:GetPanelUrl}"; Description: "Abrir el panel de administración"; Flags: postinstall shellexec nowait skipifsilent

[UninstallRun]
Filename: "{app}\RMPAYService.exe"; Parameters: "stopwait"; Flags: runhidden waituntilterminated; RunOnceId: "StopService"
Filename: "{app}\RMPAYService.exe"; Parameters: "uninstall"; Flags: runhidden waituntilterminated; RunOnceId: "RemoveService"
Filename: "{sys}\netsh.exe"; Parameters: "advfirewall firewall delete rule name=""RMPAY Service API"""; Flags: runhidden waituntilterminated; RunOnceId: "RemoveFirewallRule"

[UninstallDelete]
Type: files; Name: "{commondesktop}\Panel RMPAY.url"
Type: files; Name: "{group}\Panel de administración RMPAY.url"

[Code]
const
  ServiceId = 'RMPAYService';
  FirewallRule = 'RMPAY Service API';

var
  DbPage, RmPage, ShopifyPage, PortsPage: TInputQueryWizardPage;
  OptionsPage: TInputOptionWizardPage;
  SqlProvider: String;
  Prefilled: Boolean;

{ ───────────── Texto y archivo .properties ───────────── }

function ReplaceAll(S: String; const FromStr, ToStr: String): String;
begin
  StringChangeEx(S, FromStr, ToStr, True);
  Result := S;
end;

{ Escapa un valor para .properties: \ -> \\, no ASCII -> \uXXXX, espacio inicial -> '\ ' }
function PropEscape(const S: String): String;
var
  I: Integer;
begin
  Result := '';
  for I := 1 to Length(S) do
  begin
    if S[I] = '\' then
      Result := Result + '\\'
    else if Ord(S[I]) > 126 then
      Result := Result + '\u' + Format('%.4x', [Ord(S[I])])
    else
      Result := Result + S[I];
  end;
  if (Length(Result) > 0) and (Result[1] = ' ') then
    Result := '\' + Result;
end;

function PropUnescape(const S: String): String;
var
  I: Integer;
begin
  Result := '';
  I := 1;
  while I <= Length(S) do
  begin
    if (S[I] = '\') and (I < Length(S)) then
    begin
      if (S[I + 1] = 'u') and (I + 5 <= Length(S)) then
      begin
        Result := Result + Chr(StrToIntDef('$' + Copy(S, I + 2, 4), 63));
        I := I + 6;
      end
      else
      begin
        Result := Result + S[I + 1];
        I := I + 2;
      end;
    end
    else
    begin
      Result := Result + S[I];
      I := I + 1;
    end;
  end;
end;

function ReadProp(const Lines: TArrayOfString; const Key: String; const Default: String): String;
var
  I: Integer;
begin
  Result := Default;
  for I := 0 to GetArrayLength(Lines) - 1 do
    if Pos(Key + '=', Lines[I]) = 1 then
    begin
      Result := PropUnescape(Copy(Lines[I], Length(Key) + 2, MaxInt));
      Exit;
    end;
end;

{ Texto entre Prefix y el siguiente ';' (o el final) }
function TextAfter(const S, Prefix: String): String;
var
  P: Integer;
begin
  Result := '';
  P := Pos(Lowercase(Prefix), Lowercase(S));
  if P = 0 then
    Exit;
  Result := Copy(S, P + Length(Prefix), MaxInt);
  P := Pos(';', Result);
  if P > 0 then
    Result := Copy(Result, 1, P - 1);
end;

function UrlEncode(const S: String): String;
var
  I: Integer;
begin
  Result := '';
  for I := 1 to Length(S) do
    if ((S[I] >= 'A') and (S[I] <= 'Z')) or ((S[I] >= 'a') and (S[I] <= 'z'))
       or ((S[I] >= '0') and (S[I] <= '9')) or (S[I] = '-') or (S[I] = '_') or (S[I] = '.') or (S[I] = '~') then
      Result := Result + S[I]
    else
      Result := Result + '%' + Format('%.2x', [Ord(S[I]) and $FF]);
end;

{ Dominio de la tienda: acepta "tienda", "tienda.myshopify.com" o una URL completa }
function NormalizeShop(S: String): String;
begin
  S := Lowercase(Trim(S));
  if Pos('https://', S) = 1 then
    Delete(S, 1, 8);
  if Pos('http://', S) = 1 then
    Delete(S, 1, 7);
  if Pos('/', S) > 0 then
    S := Copy(S, 1, Pos('/', S) - 1);
  if (S <> '') and (Pos('.', S) = 0) then
    S := S + '.myshopify.com';
  Result := S;
end;

function TrimSlash(S: String): String;
begin
  S := Trim(S);
  while (Length(S) > 0) and (S[Length(S)] = '/') do
    S := Copy(S, 1, Length(S) - 1);
  Result := S;
end;

{ ───────────── Valores elegidos ───────────── }

function SqlServer: String;      begin Result := Trim(DbPage.Values[0]); end;
function SqlUser: String;        begin Result := Trim(DbPage.Values[1]); end;
function SqlPassword: String;    begin Result := DbPage.Values[2]; end;
function MainDb: String;         begin Result := Trim(DbPage.Values[3]); end;
function ShopifyDb: String;      begin Result := Trim(DbPage.Values[4]); end;
function RmUrl: String;          begin Result := TrimSlash(RmPage.Values[0]); end;
function ShopDomain: String;     begin Result := NormalizeShop(ShopifyPage.Values[0]); end;
function ApiPort: String;        begin Result := Trim(PortsPage.Values[0]); end;
function AdminPort: String;      begin Result := Trim(PortsPage.Values[1]); end;

function GetPanelUrl(Param: String): String;
begin
  Result := 'https://localhost:' + AdminPort + '/admin';
end;

{ JDBC usa host:puerto; SQL Server Management Studio usa host,puerto }
function JdbcUrl(const Db: String): String;
begin
  Result := 'jdbc:sqlserver://' + ReplaceAll(SqlServer, ',', ':') + ';database=' + Db + ';TrustServerCertificate=True';
end;

{ ───────────── SQL Server (ADO) ───────────── }

function OleValue(const S: String): String;
begin
  Result := '"' + ReplaceAll(S, '"', '""') + '"';
end;

function ConnString(const Provider, Db: String): String;
begin
  Result := 'Provider=' + Provider + ';Data Source=' + OleValue(SqlServer) + ';Initial Catalog=' + OleValue(Db)
    + ';User ID=' + OleValue(SqlUser) + ';Password=' + OleValue(SqlPassword) + ';';
  if Provider <> 'SQLOLEDB' then
    Result := Result + 'Use Encryption for Data=Optional;';
end;

{ Abre una conexión probando los proveedores OLE DB disponibles en el equipo }
function OpenSql(const Db: String; var Conn: Variant; var Err: String): Boolean;
var
  Providers: TArrayOfString;
  I: Integer;
begin
  Result := False;
  if SqlProvider <> '' then
  begin
    SetArrayLength(Providers, 1);
    Providers[0] := SqlProvider;
  end
  else
  begin
    SetArrayLength(Providers, 3);
    Providers[0] := 'MSOLEDBSQL19';
    Providers[1] := 'MSOLEDBSQL';
    Providers[2] := 'SQLOLEDB';
  end;
  for I := 0 to GetArrayLength(Providers) - 1 do
  begin
    try
      Conn := CreateOleObject('ADODB.Connection');
      Conn.ConnectionTimeout := 10;
      Conn.Open(ConnString(Providers[I], Db));
      SqlProvider := Providers[I];
      Result := True;
    except
      Err := GetExceptionMessage;
    end;
    if Result then
      Break;
  end;
end;

{ 1 = existe, 0 = no existe, -1 = no se pudo consultar }
function DatabaseExists(const Name: String): Integer;
var
  Conn, Rs: Variant;
  Err: String;
begin
  Result := -1;
  if not OpenSql('master', Conn, Err) then
    Exit;
  try
    Rs := Conn.Execute('SELECT CASE WHEN DB_ID(N''' + ReplaceAll(Name, '''', '''''') + ''') IS NULL THEN 0 ELSE 1 END');
    Result := Rs.Fields.Item(0).Value;
    Conn.Close;
  except
    Result := -1;
  end;
end;

function CreateDatabase(const Name: String; var Err: String): Boolean;
var
  Conn: Variant;
begin
  Result := False;
  if not OpenSql('master', Conn, Err) then
    Exit;
  try
    Conn.Execute('CREATE DATABASE [' + ReplaceAll(Name, ']', ']]') + ']');
    Conn.Close;
    Result := True;
  except
    Err := GetExceptionMessage;
  end;
end;

{ ───────────── HTTP ───────────── }

{ Código HTTP de la respuesta, o 0 si no hubo respuesta. Acepta certificados no confiables
  (el panel usa un certificado local). }
function HttpStatus(const Method, Url, Body: String; var ResponseText: String): Integer;
var
  Http: Variant;
begin
  Result := 0;
  ResponseText := '';
  try
    Http := CreateOleObject('MSXML2.ServerXMLHTTP.6.0');
    Http.setTimeouts(5000, 5000, 10000, 15000);
    Http.open(Method, Url, False);
    Http.setOption(2, 13056);
    if Body <> '' then
      Http.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    Http.send(Body);
    Result := Http.status;
    ResponseText := Http.responseText;
  except
    Result := 0;
    ResponseText := GetExceptionMessage;
  end;
end;

{ ───────────── Páginas del asistente ───────────── }

procedure InitializeWizard;
begin
  DbPage := CreateInputQueryPage(wpSelectDir,
    'Base de datos', 'Conexión a SQL Server',
    'Datos del SQL Server donde está la base de datos de Retail Manager. La base de datos del servicio se crea si no existe.');
  DbPage.Add('Servidor SQL (por ejemplo localhost\CSE o 192.168.1.10,1433):', False);
  DbPage.Add('Usuario de SQL Server:', False);
  DbPage.Add('Contraseña:', True);
  DbPage.Add('Base de datos de Retail Manager:', False);
  DbPage.Add('Base de datos del servicio:', False);
  DbPage.Values[0] := 'localhost\CSE';
  DbPage.Values[1] := 'sa';
  DbPage.Values[3] := 'POSDPS';
  DbPage.Values[4] := 'ShopifyServiceDB';

  RmPage := CreateInputQueryPage(DbPage.ID,
    'Retail Manager', 'RM API y productos para facturar',
    'Dirección de RM API y códigos de los productos de Retail Manager con los que se facturan el envío y la propina de los pedidos de Shopify. Esos productos deben existir en Retail Manager.');
  RmPage.Add('Dirección de RM API:', False);
  RmPage.Add('Código del producto de envío:', False);
  RmPage.Add('Código del producto de propina:', False);
  RmPage.Values[0] := 'http://localhost:8180';
  RmPage.Values[1] := 'FLETE';
  RmPage.Values[2] := 'PROPINA';

  ShopifyPage := CreateInputQueryPage(RmPage.ID,
    'Shopify', 'Tienda y credenciales de la app',
    'Datos de la tienda de Shopify y de la app personalizada (Configuración > Apps > Desarrollar apps).');
  ShopifyPage.Add('Tienda (por ejemplo mitienda.myshopify.com):', False);
  ShopifyPage.Add('Client ID:', False);
  ShopifyPage.Add('Client secret:', True);
  ShopifyPage.Add('Secreto de webhooks (opcional):', True);
  ShopifyPage.Add('Versión de la API de Shopify:', False);
  ShopifyPage.Values[4] := '2026-04';

  PortsPage := CreateInputQueryPage(ShopifyPage.ID,
    'Puertos', 'Puertos del servicio',
    'Puerto de la API del servicio y puerto del panel de administración local. Cámbielos solo si ya están en uso en este equipo.');
  PortsPage.Add('Puerto de la API:', False);
  PortsPage.Add('Puerto del panel de administración:', False);
  PortsPage.Values[0] := '9192';
  PortsPage.Values[1] := '47821';

  OptionsPage := CreateInputOptionPage(PortsPage.ID,
    'Opciones', 'Opciones de funcionamiento', '', False, False);
  OptionsPage.Add('Modo simulación: arma las facturas y las escribe en el registro, pero no las envía a Retail Manager');
  OptionsPage.Add('Permitir conexiones a la API desde otros equipos (regla del Firewall de Windows)');
  OptionsPage.Values[0] := False;
  OptionsPage.Values[1] := True;
end;

{ Al reinstalar, las páginas muestran la configuración actual }
procedure PrefillFromExistingConfig;
var
  Lines: TArrayOfString;
  Url, Server: String;
  P: Integer;
begin
  Prefilled := True;
  if not LoadStringsFromFile(WizardDirValue + '\config\application.properties', Lines) then
    Exit;

  Url := ReadProp(Lines, 'spring.datasource.db1.jdbc-url', '');
  if Url <> '' then
  begin
    Server := TextAfter(Url, 'jdbc:sqlserver://');
    if Pos('\', Server) = 0 then
      Server := ReplaceAll(Server, ':', ',');
    DbPage.Values[0] := Server;
    DbPage.Values[3] := TextAfter(Url, 'database=');
  end;
  DbPage.Values[1] := ReadProp(Lines, 'spring.datasource.db1.username', DbPage.Values[1]);
  DbPage.Values[2] := ReadProp(Lines, 'spring.datasource.db1.password', '');
  Url := ReadProp(Lines, 'spring.datasource.db2.jdbc-url', '');
  if Url <> '' then
    DbPage.Values[4] := TextAfter(Url, 'database=');

  RmPage.Values[0] := ReadProp(Lines, 'pos.base-url', RmPage.Values[0]);
  RmPage.Values[1] := ReadProp(Lines, 'shopify.shipping_item_code', RmPage.Values[1]);
  RmPage.Values[2] := ReadProp(Lines, 'shopify.tip_item_code', RmPage.Values[2]);

  Url := ReadProp(Lines, 'shopify.url', '');
  if Url <> '' then
  begin
    ShopifyPage.Values[0] := NormalizeShop(Url);
    P := Pos('/admin/api/', Url);
    if P > 0 then
    begin
      Url := Copy(Url, P + Length('/admin/api/'), MaxInt);
      if Pos('/', Url) > 0 then
        ShopifyPage.Values[4] := Copy(Url, 1, Pos('/', Url) - 1);
    end;
  end;
  ShopifyPage.Values[1] := ReadProp(Lines, 'shopify.client_id', '');
  ShopifyPage.Values[2] := ReadProp(Lines, 'shopify.client_secret', '');
  ShopifyPage.Values[3] := ReadProp(Lines, 'shopify.webhook_secret', '');

  PortsPage.Values[0] := ReadProp(Lines, 'server.port', PortsPage.Values[0]);
  PortsPage.Values[1] := ReadProp(Lines, 'admin.port', PortsPage.Values[1]);

  OptionsPage.Values[0] := Lowercase(ReadProp(Lines, 'shopify.invoice_dry_run', 'false')) = 'true';
end;

procedure CurPageChanged(CurPageID: Integer);
begin
  if (CurPageID = DbPage.ID) and not Prefilled then
    PrefillFromExistingConfig;
end;

{ ───────────── Validaciones ───────────── }

function ValidateDbPage: Boolean;
var
  Conn: Variant;
  Err: String;
  Exists: Integer;
begin
  Result := False;
  if (SqlServer = '') or (SqlUser = '') or (MainDb = '') or (ShopifyDb = '') then
  begin
    MsgBox('Complete el servidor, el usuario y las dos bases de datos.', mbError, MB_OK);
    Exit;
  end;

  SqlProvider := '';
  if not OpenSql(MainDb, Conn, Err) then
  begin
    Result := MsgBox('No se pudo conectar a la base de datos "' + MainDb + '":' + #13#10#13#10 + Err
      + #13#10#13#10 + '¿Continuar de todos modos?', mbError, MB_YESNO or MB_DEFBUTTON2) = IDYES;
    Exit;
  end;
  Conn.Close;

  Exists := DatabaseExists(ShopifyDb);
  if Exists = 0 then
  begin
    if MsgBox('La base de datos "' + ShopifyDb + '" no existe en el servidor.' + #13#10#13#10
      + '¿Crearla ahora?', mbConfirmation, MB_YESNO) <> IDYES then
    begin
      MsgBox('El servicio necesita esa base de datos para funcionar.', mbError, MB_OK);
      Exit;
    end;
    if not CreateDatabase(ShopifyDb, Err) then
    begin
      MsgBox('No se pudo crear la base de datos "' + ShopifyDb + '":' + #13#10#13#10 + Err, mbError, MB_OK);
      Exit;
    end;
  end;

  if not OpenSql(ShopifyDb, Conn, Err) then
  begin
    Result := MsgBox('No se pudo conectar a la base de datos "' + ShopifyDb + '":' + #13#10#13#10 + Err
      + #13#10#13#10 + '¿Continuar de todos modos?', mbError, MB_YESNO or MB_DEFBUTTON2) = IDYES;
    Exit;
  end;
  Conn.Close;
  Result := True;
end;

function ValidateRmPage: Boolean;
var
  Response: String;
begin
  Result := False;
  if (Pos('http://', Lowercase(RmUrl)) <> 1) and (Pos('https://', Lowercase(RmUrl)) <> 1) then
  begin
    MsgBox('La dirección de RM API debe empezar con http:// o https://', mbError, MB_OK);
    Exit;
  end;
  if (Trim(RmPage.Values[1]) = '') or (Trim(RmPage.Values[2]) = '') then
  begin
    MsgBox('Indique los códigos de los productos de envío y de propina.', mbError, MB_OK);
    Exit;
  end;
  if HttpStatus('GET', RmUrl + '/cse.api.v1/HealthCheck', '', Response) <> 200 then
  begin
    Result := MsgBox('RM API no responde en ' + RmUrl + '.' + #13#10#13#10
      + 'El servicio funcionará cuando RM API esté disponible. ¿Continuar?', mbConfirmation, MB_YESNO) = IDYES;
    Exit;
  end;
  Result := True;
end;

function ValidateShopifyPage: Boolean;
var
  Response, Body: String;
begin
  Result := False;
  if (ShopDomain = '') or (Trim(ShopifyPage.Values[1]) = '') or (Trim(ShopifyPage.Values[2]) = '')
     or (Trim(ShopifyPage.Values[4]) = '') then
  begin
    MsgBox('Complete la tienda, el Client ID, el Client secret y la versión de la API.', mbError, MB_OK);
    Exit;
  end;
  Body := 'grant_type=client_credentials&client_id=' + UrlEncode(Trim(ShopifyPage.Values[1]))
    + '&client_secret=' + UrlEncode(Trim(ShopifyPage.Values[2]));
  if (HttpStatus('POST', 'https://' + ShopDomain + '/admin/oauth/access_token', Body, Response) <> 200)
     or (Pos('access_token', Response) = 0) then
  begin
    Result := MsgBox('Shopify no aceptó estas credenciales para ' + ShopDomain + ':' + #13#10#13#10
      + Copy(Response, 1, 300) + #13#10#13#10 + '¿Continuar de todos modos?', mbError, MB_YESNO or MB_DEFBUTTON2) = IDYES;
    Exit;
  end;
  Result := True;
end;

function ValidatePortsPage: Boolean;
var
  Api, Admin: Integer;
begin
  Result := False;
  Api := StrToIntDef(ApiPort, 0);
  Admin := StrToIntDef(AdminPort, 0);
  if (Api < 1) or (Api > 65535) or (Admin < 1) or (Admin > 65535) then
  begin
    MsgBox('Los puertos deben ser números entre 1 y 65535.', mbError, MB_OK);
    Exit;
  end;
  if Api = Admin then
  begin
    MsgBox('La API y el panel deben usar puertos distintos.', mbError, MB_OK);
    Exit;
  end;
  Result := True;
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  Result := True;
  if CurPageID = DbPage.ID then
    Result := ValidateDbPage
  else if CurPageID = RmPage.ID then
    Result := ValidateRmPage
  else if CurPageID = ShopifyPage.ID then
    Result := ValidateShopifyPage
  else if CurPageID = PortsPage.ID then
    Result := ValidatePortsPage;
end;

{ ───────────── Instalación ───────────── }

function ServiceExists: Boolean;
var
  Code: Integer;
begin
  Result := Exec(ExpandConstant('{sys}\sc.exe'), 'query ' + ServiceId, '', SW_HIDE, ewWaitUntilTerminated, Code)
    and (Code = 0);
end;

function RunHidden(const FileName, Params: String): Integer;
begin
  if not Exec(FileName, Params, '', SW_HIDE, ewWaitUntilTerminated, Result) then
    Result := -1;
end;

{ En una actualización, el servicio se detiene antes de reemplazar los archivos }
function PrepareToInstall(var NeedsRestart: Boolean): String;
var
  Wrapper: String;
begin
  Result := '';
  if ServiceExists then
  begin
    Wrapper := WizardDirValue + '\RMPAYService.exe';
    if FileExists(Wrapper) then
      RunHidden(Wrapper, 'stopwait')
    else
      RunHidden(ExpandConstant('{sys}\sc.exe'), 'stop ' + ServiceId);
  end;
end;

procedure WriteConfig;
var
  Lines: TArrayOfString;
begin
  SetArrayLength(Lines, 22);
  Lines[0] := '# RMPAY Service - configuracion generada por el instalador (version {#AppVersion}).';
  Lines[1] := '# Para cambiarla, vuelva a ejecutar el instalador o edite este archivo y reinicie el servicio RMPAYService.';
  Lines[2] := 'server.port=' + PropEscape(ApiPort);
  Lines[3] := 'admin.port=' + PropEscape(AdminPort);
  Lines[4] := 'spring.datasource.db1.jdbc-url=' + PropEscape(JdbcUrl(MainDb));
  Lines[5] := 'spring.datasource.db1.username=' + PropEscape(SqlUser);
  Lines[6] := 'spring.datasource.db1.password=' + PropEscape(SqlPassword);
  Lines[7] := 'spring.datasource.db2.jdbc-url=' + PropEscape(JdbcUrl(ShopifyDb));
  Lines[8] := 'spring.datasource.db2.username=' + PropEscape(SqlUser);
  Lines[9] := 'spring.datasource.db2.password=' + PropEscape(SqlPassword);
  Lines[10] := 'pos.base-url=' + PropEscape(RmUrl);
  Lines[11] := 'shopify.shipping_item_code=' + PropEscape(Trim(RmPage.Values[1]));
  Lines[12] := 'shopify.tip_item_code=' + PropEscape(Trim(RmPage.Values[2]));
  Lines[13] := 'shopify.url=' + PropEscape('https://' + ShopDomain + '/admin/api/' + Trim(ShopifyPage.Values[4]) + '/graphql.json');
  Lines[14] := 'shopify.urlAccesToken=' + PropEscape('https://' + ShopDomain + '/admin/oauth/access_token');
  Lines[15] := 'shopify.client_id=' + PropEscape(Trim(ShopifyPage.Values[1]));
  Lines[16] := 'shopify.client_secret=' + PropEscape(Trim(ShopifyPage.Values[2]));
  Lines[17] := 'shopify.webhook_secret=' + PropEscape(Trim(ShopifyPage.Values[3]));
  if OptionsPage.Values[0] then
    Lines[18] := 'shopify.invoice_dry_run=true'
  else
    Lines[18] := 'shopify.invoice_dry_run=false';
  Lines[19] := '# Registro: sin el detalle de cada consulta SQL';
  Lines[20] := 'logging.level.org.hibernate.SQL=warn';
  Lines[21] := '';
  SaveStringsToFile(ExpandConstant('{app}\config\application.properties'), Lines, False);
end;

{ La configuración tiene contraseñas: solo Administradores y SYSTEM pueden leerla }
procedure ProtectConfig;
begin
  RunHidden(ExpandConstant('{sys}\icacls.exe'),
    '"' + ExpandConstant('{app}\config') + '" /inheritance:r /grant:r *S-1-5-32-544:(OI)(CI)F *S-1-5-18:(OI)(CI)F');
end;

procedure ConfigureFirewall;
var
  Netsh: String;
begin
  Netsh := ExpandConstant('{sys}\netsh.exe');
  RunHidden(Netsh, 'advfirewall firewall delete rule name="' + FirewallRule + '"');
  if OptionsPage.Values[1] then
    RunHidden(Netsh, 'advfirewall firewall add rule name="' + FirewallRule + '" dir=in action=allow protocol=TCP localport=' + ApiPort);
end;

{ Espera a que el panel responda (Spring Boot tarda en arrancar) }
procedure WaitForPanel;
var
  I: Integer;
  Response: String;
begin
  WizardForm.StatusLabel.Caption := 'Iniciando el servicio...';
  for I := 1 to 60 do
  begin
    if HttpStatus('GET', GetPanelUrl('') + '/index.html', '', Response) > 0 then
      Exit;
    Sleep(2000);
  end;
  MsgBox('El servicio quedó instalado, pero el panel todavía no responde.' + #13#10#13#10
    + 'Revise los registros en ' + ExpandConstant('{app}\logs') + '.', mbInformation, MB_OK);
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  Wrapper: String;
begin
  if CurStep <> ssPostInstall then
    Exit;

  WizardForm.StatusLabel.Caption := 'Guardando la configuración...';
  WriteConfig;
  ProtectConfig;

  WizardForm.StatusLabel.Caption := 'Registrando el servicio de Windows...';
  Wrapper := ExpandConstant('{app}\RMPAYService.exe');
  if not ServiceExists then
    RunHidden(Wrapper, 'install');
  ConfigureFirewall;
  RunHidden(Wrapper, 'start');
  WaitForPanel;
end;

{ ───────────── Desinstalación ───────────── }

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  if (CurUninstallStep = usPostUninstall) and DirExists(ExpandConstant('{app}')) then
    if SuppressibleMsgBox('¿Eliminar también la configuración y los registros del servicio?' + #13#10#13#10
      + ExpandConstant('{app}'), mbConfirmation, MB_YESNO, IDNO) = IDYES then
      DelTree(ExpandConstant('{app}'), True, True, True);
end;
