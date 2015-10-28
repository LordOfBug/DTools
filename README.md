# DTools
Developer tools (SSH, FTP, SNMP simulator, automatization of common tasks, network status...)

This application is still in development phase.

For a developer it should help to simplify common tasks
- create a network of servers
- monitor network
- execute ssh commands
- upload/download files using ftp/sftp
- snmp manager
- snmp agent
- snmp simulator
- quick notes
- todo list
- external links, favorites



Commons-logging is required by commons-vfs

-----------------------------------------------------------
0.4.0-beta.03

28. october 2015

- added button for adding new trap scenarios

-----------------------------------------------------------

0.4.0-beta.02

27. october 2015

- code cleaning
- bug fixes

-----------------------------------------------------------

0.4.0-beta.01

24. october 2015

- fixed saving traps
- fixed senderThread
- the same functionality as in 0.3.0 should work (except that files are no more compatible)

-----------------------------------------------------------

0.4.0-alpha.02

23. october 2015

- show trap lists per agent

-----------------------------------------------------------

0.4.0-alpha.01

21. october 2015

- redefined snmp directory structure (previous xml files are no more compatible)
- fixed adding, deleting and modifying agent data

-----------------------------------------------------------

0.3.0

16. october 2015

- 0.3.0 released
- bug fixes

-----------------------------------------------------------

0.3.0-beta.01

14. october 2015

- updater: fixed moving files and directories
- updater: added copy functionality for files and directories

-----------------------------------------------------------
0.3.0-alpha.02

8. october 2015

- agent simulator
- multiple agents (still with the same trap list)

-----------------------------------------------------------

0.3.0-alpha.01

02. october 2015

- snmp manager with many trap receivers
- trap monitor and trap viewer (in raw format)
- added login xhtml page, but it is not implemented
- introduction of users (currently only default user)
- updater - update based on install-script.xml
- moved configuration files to users directory
- scripts moved to bin
- bug fixes in snmp

-----------------------------------------------------------

0.2.2

21. september 2015

- snmp monitor: select rows to show

-----------------------------------------------------------

0.2.1

07. september 2015

- sender thread - send traps in specified interval
- trap info (trap monitor)
- changed format of snmpTraps.xml (varbinds contain attributes rather than elements) - to convert use xmlConverter.jar

-----------------------------------------------------------

0.2.0

03. september 2015

- added GNU GPL license
- javascript rules for traps
- trap monitor (colors based on severities)
- periodic polling (trap monitor)
- show network interfaces (in raw format)
- check and save properties after loading

-----------------------------------------------------------

0.1.0

31. july 2015

- network (servers, ping)
- ssh
- ftp
- snmp client
- trap receiver
- trap sender
- todos
- notes


