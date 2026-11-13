USE [ShopifyServiceDB]

SET IDENTITY_INSERT [dbo].[Sys_general_config] ON 
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (1, N'config.isFirstRun', N'IS FIRST RUN', N'true')
SET IDENTITY_INSERT [dbo].[Sys_general_config] OFF