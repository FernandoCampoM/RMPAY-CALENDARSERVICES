USE [RMPAY]
GO
SET IDENTITY_INSERT [dbo].[BusinessConfiguration] ON 
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (1, N'Tip.Tip1', CAST(N'2024-09-25T23:18:48.1694570' AS DateTime2), CAST(N'2024-09-25T23:18:48.1694570' AS DateTime2), N'100', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (2, N'Tip.Tip2', CAST(N'2024-09-25T23:20:42.0197970' AS DateTime2), CAST(N'2024-09-25T23:20:42.0197970' AS DateTime2), N'100', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (4, N'Tip.Tip3', CAST(N'2024-09-25T23:29:06.4248080' AS DateTime2), CAST(N'2024-09-25T23:29:06.4248080' AS DateTime2), N'100', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (5, N'IvuControl.ActivateFiscalTerminal', CAST(N'2024-09-25T23:32:21.2822950' AS DateTime2), CAST(N'2024-09-25T23:32:21.2822950' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (6, N'IvuControl.SURI', CAST(N'2024-09-25T23:33:50.7446400' AS DateTime2), CAST(N'2024-09-25T23:33:50.7446400' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (7, N'PayMethods.CASH.active', CAST(N'2024-09-25T23:39:11.7502750' AS DateTime2), CAST(N'2024-09-25T23:39:11.7502750' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (8, N'PayMethods.CASH.OpenCashDrawer', CAST(N'2024-09-25T23:40:33.8939950' AS DateTime2), CAST(N'2024-09-25T23:40:33.8939950' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (9, N'PayMethods.DEBIT.active', CAST(N'2024-09-25T23:39:11.7502750' AS DateTime2), CAST(N'2024-09-25T23:39:11.7502750' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (10, N'PayMethods.DEBIT.OpenCashDrawer', CAST(N'2024-09-25T23:40:33.8939950' AS DateTime2), CAST(N'2024-09-25T23:40:33.8939950' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (11, N'PayMethods.CREDIT.active', CAST(N'2024-09-25T23:39:11.7502750' AS DateTime2), CAST(N'2024-09-25T23:39:11.7502750' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (12, N'PayMethods.CREDIT.OpenCashDrawer', CAST(N'2024-09-25T23:40:33.8939950' AS DateTime2), CAST(N'2024-09-25T23:40:33.8939950' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (13, N'PayMethods.ATHMOVIL.active', CAST(N'2024-09-25T23:39:11.7502750' AS DateTime2), CAST(N'2024-09-25T23:39:11.7502750' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (14, N'PayMethods.ATHMOVIL.OpenCashDrawer', CAST(N'2024-09-25T23:40:33.8939950' AS DateTime2), CAST(N'2024-09-25T23:40:33.8939950' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (15, N'ReceiptHeader.Line1', CAST(N'2024-09-25T23:56:59.6934700' AS DateTime2), CAST(N'2024-09-25T23:56:59.6934700' AS DateTime2), N'---------------------------', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (16, N'ReceiptHeader.Line2', CAST(N'2024-09-25T23:57:05.2341780' AS DateTime2), CAST(N'2024-09-25T23:57:05.2341780' AS DateTime2), N'---------------------------', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (17, N'ReceiptHeader.Line3', CAST(N'2024-09-25T23:57:09.3759950' AS DateTime2), CAST(N'2024-09-25T23:57:09.3759950' AS DateTime2), N'---------------------------', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (18, N'ReceiptHeader.Line4', CAST(N'2024-09-25T23:57:18.6564290' AS DateTime2), CAST(N'2024-09-25T23:57:18.6564290' AS DateTime2), N'---------------------------', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (19, N'ReceiptHeader.FootnoteCommentary', CAST(N'2024-09-25T23:57:57.4553000' AS DateTime2), CAST(N'2024-09-25T23:57:57.4553000' AS DateTime2), N'---------------------------', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (20, N'Functions.ShiftMatching', CAST(N'2024-09-25T23:59:10.4025540' AS DateTime2), CAST(N'2024-09-26T01:16:44.9424080' AS DateTime2), N'False', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (21, N'Functions.OverNightShift', CAST(N'2024-09-26T00:00:17.2009600' AS DateTime2), CAST(N'2024-09-26T01:16:44.9974110' AS DateTime2), N'False', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (22, N'Functions.exitAfterSale', CAST(N'2024-09-26T00:02:27.5743190' AS DateTime2), CAST(N'2024-09-26T01:16:45.0084120' AS DateTime2), N'False', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (23, N'Functions.ShiftsMultiterminalSales', CAST(N'2024-09-26T00:08:27.4505650' AS DateTime2), CAST(N'2024-09-26T01:16:45.0184110' AS DateTime2), N'False', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (24, N'Email.User', CAST(N'2024-09-26T00:10:46.3255990' AS DateTime2), CAST(N'2024-09-26T00:10:46.3255990' AS DateTime2), N'juancampo201509@gmail.com', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (25, N'Email.Pass', CAST(N'2024-09-26T00:11:14.6528350' AS DateTime2), CAST(N'2024-09-26T00:11:14.6528350' AS DateTime2), N'57917817cmj****', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (26, N'Email.ShiftMatching.active', CAST(N'2024-09-26T00:12:03.5526810' AS DateTime2), CAST(N'2024-09-26T00:12:03.5526810' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (27, N'Email.ShiftMatching.time', CAST(N'2024-09-26T00:13:15.0752260' AS DateTime2), CAST(N'2024-09-26T00:13:15.0752260' AS DateTime2), N'19', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (28, N'Email.DailySummary.time', CAST(N'2024-09-26T00:14:10.3340940' AS DateTime2), CAST(N'2024-09-26T00:14:10.3340940' AS DateTime2), N'19', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (29, N'Email.DailySummary.active', CAST(N'2024-09-26T00:14:18.7318120' AS DateTime2), CAST(N'2024-09-26T00:14:18.7318120' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (30, N'Email.LowInventory.active', CAST(N'2024-09-26T00:15:45.2000670' AS DateTime2), CAST(N'2024-09-26T00:15:45.2000670' AS DateTime2), N'True', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (31, N'Email.LowInventory.time', CAST(N'2024-09-26T00:15:57.5997750' AS DateTime2), CAST(N'2024-09-26T00:15:57.5997750' AS DateTime2), N'19', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (32, N'Email.BatchClosure.time', CAST(N'2024-09-26T00:16:53.2306840' AS DateTime2), CAST(N'2024-09-26T00:16:53.2306840' AS DateTime2), N'19', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (33, N'Email.BatchClosure.active', CAST(N'2024-09-26T00:16:59.4090140' AS DateTime2), CAST(N'2024-09-26T00:16:59.4100010' AS DateTime2), N'19', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (34, N'Pay@Table.PaymentTable.IpAddress', CAST(N'2024-09-26T00:19:29.4564740' AS DateTime2), CAST(N'2024-09-26T00:19:29.4564740' AS DateTime2), N'192.168.0.1', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (35, N'Pay@Table.PaymentTable.Port', CAST(N'2024-09-26T00:19:38.8570470' AS DateTime2), CAST(N'2024-09-26T00:19:38.8570470' AS DateTime2), N'9096', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (36, N'Pay@Table.RMConnect.Port', CAST(N'2024-09-26T00:20:13.7514060' AS DateTime2), CAST(N'2024-09-26T00:20:13.7514060' AS DateTime2), N'9094', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (37, N'Pay@Table.RMConnect.IpAddress', CAST(N'2024-09-26T00:20:28.6535530' AS DateTime2), CAST(N'2024-09-26T00:20:28.6535530' AS DateTime2), N'192.168.0.1', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (38, N'Pay@Table.RMConnect.Username', CAST(N'2024-09-26T00:20:41.6470930' AS DateTime2), CAST(N'2024-09-26T00:20:41.6470930' AS DateTime2), N'juancamm', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (39, N'Pay@Table.RMConnect.Password', CAST(N'2024-09-26T00:20:53.5281750' AS DateTime2), CAST(N'2024-09-26T00:20:53.5281750' AS DateTime2), N'juancamm', 33, NULL)
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (42, N'PayMethods.Option1.active', CAST(N'2024-09-26T00:00:00.0000000' AS DateTime2), CAST(N'2024-09-26T00:00:00.0000000' AS DateTime2), N'False', 33, N'Opción 1')
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (45, N'PayMethods.Option2.active', CAST(N'2024-09-26T00:00:00.0000000' AS DateTime2), CAST(N'2024-09-26T00:00:00.0000000' AS DateTime2), N'False', 33, N'Opcion 2')
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (46, N'PayMethods.Option1.OpenCashDrawer', CAST(N'2024-09-29T00:00:00.0000000' AS DateTime2), CAST(N'2024-09-26T00:00:00.0000000' AS DateTime2), N'False', 33, N'Abrir Cajon Monedero')
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (47, N'PayMethods.Option2.OpenCashDrawer', CAST(N'2024-09-29T00:00:00.0000000' AS DateTime2), CAST(N'2024-09-26T00:00:00.0000000' AS DateTime2), N'False', 33, N'Abrir Cajon Monedero')
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (50, N'PayMethods.Option1.TaxExempt', CAST(N'2024-09-29T00:00:00.0000000' AS DateTime2), CAST(N'2024-09-29T00:00:00.0000000' AS DateTime2), N'False', 33, N'Excento de Impuesto')
GO
INSERT [dbo].[BusinessConfiguration] ([Configurationid], [configKey], [createdAt], [updatedAt], [value], [businessId], [configName]) VALUES (52, N'PayMethods.Option2.TaxExempt', CAST(N'2024-09-29T00:00:00.0000000' AS DateTime2), CAST(N'2024-09-29T00:00:00.0000000' AS DateTime2), N'False', 33, N'Excento de Impuesto')
GO
SET IDENTITY_INSERT [dbo].[BusinessConfiguration] OFF
GO
