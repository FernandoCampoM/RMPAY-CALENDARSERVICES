USE [RMPAY]
SET IDENTITY_INSERT [dbo].[Users] ON 
INSERT [dbo].[Users] ([userID], [email], [enabled], [name], [password], [phone], [rol], [username]) VALUES (1, N'rmpayadmin@rmpay.com', 1, N'nombre_completo', N'$2a$10$v9Z35MgLhhMcTb6FADuliuRbHij78SRL1YShMu/oagQR0zzLUz2pG', N'3156837054', N'ROLE_MANAGER', N'rmpayadmin')
INSERT [dbo].[Users] ([userID], [email], [enabled], [name], [password], [phone], [rol], [username]) VALUES (6, N'rmpayuser@rmpay.com', 1, N'user test', N'$2a$10$v9Z35MgLhhMcTb6FADuliuRbHij78SRL1YShMu/oagQR0zzLUz2pG', N'3156837054', N'ROLE_USER', N'rmpayuser')
SET IDENTITY_INSERT [dbo].[Users] OFF
INSERT [dbo].[paymentMethods] ([code], [name], [enable], [Notes]) VALUES (N'ATHMOVIL', N'ATH Movil', 1, NULL)
INSERT [dbo].[paymentMethods] ([code], [name], [enable], [Notes]) VALUES (N'BANK-ACCOUNT', N'Cuenta de Banco', 1, NULL)
INSERT [dbo].[paymentMethods] ([code], [name], [enable], [Notes]) VALUES (N'CREDIT-CARD', N'Tarjeta de Credito', 1, NULL)



SET IDENTITY_INSERT [dbo].[Sys_general_config] ON 
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (1, N'config.blackstone.AppKey', N'AppKey', N'12345')
-- Insertar la URL
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) 
VALUES (2, N'config.blackstone.URL', N'URL', N'https://services.bmspay.com/testing/api/Transactions/sale');
-- Insertar AppType
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) 
VALUES (3, N'config.blackstone.AppType', N'AppType', N'1');
-- Insertar MID
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) 
VALUES (4, N'config.blackstone.MID', N'MID', N'76074');
-- Insertar CID
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) 
VALUES (5, N'config.blackstone.CID', N'CID', N'260');
-- Insertar Username
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) 
VALUES (6, N'config.blackstone.Username', N'Username', N'nicolas');
-- Insertar Password
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) 
VALUES (7, N'config.blackstone.Password', N'Password', N'password1');
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (9, N'config.email.AppKey', N'key', N'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')

INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (10, N'config.email.emailFrom', N'emailFrom', N'brayam.otero@somos.biz')

INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (11, N'config.email.emailTo', N'emailTo', N'juancampo201509@gmail.com')

INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (12, N'config.email.emailCCO', N'emailCCO', N'juancamm@unicauca.edu.co')
SET IDENTITY_INSERT [dbo].[Sys_general_config] OFF
SET IDENTITY_INSERT [dbo].[Service] ON 
GO
INSERT [dbo].[Service] ([serviceId], [duration], [enable], [referralPayment], [referralPayment10], [referralPayment2to5], [referralPayment6to9], [serviceDescription], [serviceName], [serviceValue], [terminals10], [terminals2to5], [terminals6to9]) VALUES (1, 31, 1, 2, 0, 0, 0, N'RM pay - Mensual', N'RM pay - Mensual', 6.99, 4.5, 5, 4.75)
GO
INSERT [dbo].[Service] ([serviceId], [duration], [enable], [referralPayment], [referralPayment10], [referralPayment2to5], [referralPayment6to9], [serviceDescription], [serviceName], [serviceValue], [terminals10], [terminals2to5], [terminals6to9]) VALUES (2, 66, 1, 5, 0, 0, 0, N'RM pay - 6 meses', N'RM pay - 6 meses', 36, 20, 40, 30)
GO
INSERT [dbo].[Service] ([serviceId], [duration], [enable], [referralPayment], [referralPayment10], [referralPayment2to5], [referralPayment6to9], [serviceDescription], [serviceName], [serviceValue], [terminals10], [terminals2to5], [terminals6to9]) VALUES (5, 365, 1, 15, 0, 0, 0, N'RM pay - anual', N'RM pay - anual', 65, 65, 65, 65)
GO
SET IDENTITY_INSERT [dbo].[Service] OFF
