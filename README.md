# assEditor

**Class:** A Softer Space Internal

**Language:** Java

**Platform:** Windows or Linux Desktop

This is an editor for our internal use at a softer space, but it should be generic enough such that others might benefit from it too.
The main idea is to get it to be fast and full of useful features - with "fast" always being the more important of these two directives. ;)

## Setup

Download our Toolbox-Java (which is a separate project here on github) into an adjacent directory on your hard drive.

Start the build by calling under Windows:

```
build.bat
```

Or under Linux:

```
./build.sh
```

## Run

To start up the assEditor after it has been built, you can call under Windows:

```
assEditor.bat
```

Or under Linux:

```
./assEditor.sh
```

## Commandline Options

You can use the following commandline options:

```
--standalone .. the editor will be started with an empty workspace with only one empty file loaded, rather than loading the last opened workspace
--version .. display the version of the assEditor, then exit
--version_for_zip .. display the version of the assEditor (in short form), then exit
```

Other commandline arguments will be interpreted as the names of files which you want to open in the current workspace.

## Alias

You can add an alias to bash under Linux to call this more easily, e.g.:

```
alias ae='/absolute/path/to/assEditor/assEditor.sh'
alias aes='/absolute/path/to/assEditor/assEditor.sh --standalone'
```

If you put this line into ~/.bashrc, the aliasses ae and aes will be available in every bash session.

## License

We at A Softer Space really love the Unlicense, which pretty much allows anyone to do anything with this source code.
For more info, see the file UNLICENSE.

If you desperately need to use this source code under a different license, [contact us](mailto:info@asofterspace.com) - I am sure we can figure something out.
