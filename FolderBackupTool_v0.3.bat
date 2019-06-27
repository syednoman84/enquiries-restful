echo off 

rem This software packs the content of the folder it is executed from
rem (files, folders and subfolders) in a ZIP file.

rem You can exclude files and folders listing them in as arguments 
rem (in place of "exclude"). Names of the files to be excluded should
rem be listed with extensions (.txt, .pdf etc.).

rem The content of the result ZIP file include
rem this file: FolderBackupTool_v0.2a.bat
rem and the original: FolderBackupTool_v0.2a.jar
rem (this can be altered if needed)

rem The name of the ZIP file is composed of:
rem FOLDERNAME + _backup_ + dateandtime + .zip

rem Options to copy/move the result ZIP file to a folder 
rem given as an argument (along with excluded files) 
rem can be added later if needed.



java -jar FolderBackupTool_v0.3.jar .git dataindexdefault release target