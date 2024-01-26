USE [RMPAY]

SET IDENTITY_INSERT [dbo].[Users] ON 
GO
INSERT [dbo].[Users] ([userID], [email], [enabled], [name], [password], [phone], [rol], [username]) VALUES (1, N'rmpayadmin@rmpay.com', 1, N'nombre_completo', N'$2a$10$v9Z35MgLhhMcTb6FADuliuRbHij78SRL1YShMu/oagQR0zzLUz2pG', N'3156837054', N'ROLE_MANAGER', N'rmpayadmin')
GO
INSERT [dbo].[Users] ([userID], [email], [enabled], [name], [password], [phone], [rol], [username]) VALUES (6, N'rmpayuser@rmpay.com', 1, N'user test', N'$2a$10$v9Z35MgLhhMcTb6FADuliuRbHij78SRL1YShMu/oagQR0zzLUz2pG', N'3156837054', N'ROLE_USER', N'rmpayuser')
GO
SET IDENTITY_INSERT [dbo].[Users] OFF
