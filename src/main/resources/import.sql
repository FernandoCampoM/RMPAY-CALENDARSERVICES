USE [RMPAY]
SET IDENTITY_INSERT [dbo].[Address] ON 
INSERT [dbo].[Address] ([addressId], [street], [town], [zipcode]) VALUES (1, N'nombre_de_la_calle', N'nombre_del_pueblo', N'codigo_postal')
SET IDENTITY_INSERT [dbo].[Address] OFF
SET IDENTITY_INSERT [dbo].[Users] ON 
INSERT [dbo].[Users] ([userID], [businessName], [email], [name], [password], [phone], [roles], [addressId], [enabled], [username]) VALUES (5, N'nombre_del_negocio', N'rmpayadmin@rmpay.com', N'nombre_completo', N'$2a$10$v9Z35MgLhhMcTb6FADuliuRbHij78SRL1YShMu/oagQR0zzLUz2pG', N'numero_de_telefono', N'ROLE_MANAGER', 1, 1, N'rmpayadmin')
SET IDENTITY_INSERT [dbo].[Users] OFF
