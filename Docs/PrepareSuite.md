PrepareSuite
============

Abstract
--------

This document describes PrepareSuite script, the part of Lorem Ipsum project. The aim of the script is to build task suite packages and, optionally, upload them automatically into task servers.

Requirements
------------

The script requires Python 3.3 (or newer) to be installed on machine where build process is executed.

For automatic upload feature to work, target machine must have available access through SSH protocol. SSH access should be available without password (not recommended), or using private key.

General information
-------------------

As such, task suite package is simply a ZIP archive containing test's data structure. However, usual ZIP tools should not be used to create it, as Lorem Ipsum application expects to special control file to be available in archive for purposes of download errors detection. This is the reason, why suite preparation script should be used to build packages.


Invocation
----------

### Invocation scheme

```
prepare.py [-h] --suite-directory SUITE_DIRECTORY --suite-version
                  SUITE_VERSION [--target-server TARGET_SERVER]
                  [--target-server-ssh-key TARGET_SERVER_SSH_KEY]
                  [--target-path TARGET_PATH]
```

### Options:

`-h`, `--help`

Show help message and exit
  
`--suite-directory SUITE_DIRECTORY`

Directory containing task suite data. Please remember, that this name will be used as part of package name, and thus, the name visible for user that will install task suite.

`--suite-version SUITE_VERSION`

Task suite version. *Must not* contain `-` (*hypen*/*minus* character).

`--target-server TARGET_SERVER`

Target server to upload suite to. Server must have access through SSH protocol. SSH access must be available without password (not recommended), or using private key. Private key must then be passed to "--target-server-ssh-key" option. If this option is not passed, package will not be automatically uploaded.

`--target-server-ssh-key TARGET_SERVER_SSH_KEY`

SSH Private Key that shall be used to access server passed in `--target-server` option. This must be RSA Private Key, which matching public key should be installed on target machine. If this option is not passed, it is assumed that target does not require any user verification (not recommended configuration), or that user is ready to manually type the password many times.

`--target-path TARGET_PATH`

Path on target server to upload suite to. By default, user's home directory is used.

Example
-------

`./prepare.py --suite-directory "First Test Suite" --suite-version "1.37c"`

Will grab all files from "First Test Suite" directory, build a package and write it in the current directory. As a result, we now have file called `First Test Suite-1.37c.zip` which is ready to submit to task server.

`./prepare.py --suite-directory "Second Test Suite" --suite-version "3.17" --target-server "charlie@example.org:22" --target-server-ssh-key "server.id_rsa" --target-path="task_server/suites"`

Will build the package using content of `Second Test Suite`, build a package (full name will be `Second Test Suite-1.37c.zip`) and upload it automatically to the server at `example.org`. Connection will be done on port `22` as user `charlie`. Package will be put into `task_server/suites` directory in Charlie's home path.