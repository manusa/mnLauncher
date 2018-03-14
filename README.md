# mnLauncher - Simple Groovy application launcher

[![Build Status](https://travis-ci.org/manusa/mnLauncher.svg?branch=master)](https://travis-ci.org/manusa/mnLauncher)

## Description

mnLauncher is a simple application launcher written in Groovy.

The aim of the application is to allow the execution of regular commands/scripts/applications through a simple popup menu
that gets displayed whenever you right-click the application's icon which will always be in hand in the foreground.
You can move the icon around or out of the way, but keep it at hand to run your command whenever you need it.

The application menu is defined in a separate file in JSON format. Take a look to the sample **menu.json** to see
how the file is structured.

## Customization

You can change the launcher icon by placing a PNG file named 'favicon.png' in the same directory you store the
application jar (runtime/working directory).
