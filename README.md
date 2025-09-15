
<h1 align="center">
  <br>
  <img width="100" alt="datapack_color" src="https://github.com/user-attachments/assets/3e91b714-e6f9-4cc2-a25f-c22404b78e52" />
  <br>
  DataPack-IDE
</h1>
<h4 align="center">A work in progress minecraft ingame IDE mod for datapack development.</h4>
<div align="center">  

  [![Discord](https://img.shields.io/discord/1163847082080211025?label=discord&color=9089DA&logo=discord&style=for-the-badge)](https://discord.com/invite/qZ885qTvkx)
  [![Downloads](https://img.shields.io/github/downloads/Frostzie/DataPack-IDE/total?label=downloads&color=208a19&logo=github&style=for-the-badge)](https://github.com/Frostzie/DataPack-IDE/releases)
  
  [![Fabric](https://img.shields.io/badge/Fabric-0.131.0+1.21.8-blue.svg?logo=data:image/svg%2bxml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxNiIgaGVpZ2h0PSIxNiIgdmlld0JveD0iMCAwIDE2IDE2Ij48cGF0aCBmaWxsPSIjMzgzNDJhIiBkPSJNOSAxaDF2MWgxdjFoMXYxaDF2MWgxdjFoMXYyaC0xdjFoLTJ2MWgtMXYxaC0xdjFIOXYySDh2MUg2di0xSDV2LTFINHYtMUgzdi0xSDJWOWgxVjhoMVY3aDFWNmgxVjVoMVY0aDFWMmgxeiIvPjxwYXRoIGZpbGw9IiNkYmQwYjQiIGQ9Ik00IDlWOGgxVjdoMVY2aDFsMS0xVjRoMVYyaDF2MWgxdjFoMXYxaDF2MWwtMSAxLTIgMy0zIDMtMy0zeiIvPjxwYXRoIGZpbGw9IiNiY2IyOWMiIGQ9Ik05IDNoMXYxaDF2MWgxdjFoMXYxaC0xTDkgNHpNMTAgMTBoMVY5aDFWN2gtMXYxaC0xekg4djJoMXYtMWgxek04IDEySDd2MWgxeiIvPjxwYXRoIGZpbGw9IiNjNmJjYTUiIGQ9Ik03IDVoMXYyaDN2MUg5VjZIN3pNNiA4aDF2MmgyVjlINnoiLz48cGF0aCBmaWxsPSIjYWVhNjk0IiBkPSJNMyA5djFsMyAzaDF2LTFINnYtMUg1di0xSDRWOXoiLz48cGF0aCBmaWxsPSIjOWE5MjdlIiBkPSJNMyAxMHYxaDJ2MmgydjFINnYtMkg0di0yeiIvPjxwYXRoIGZpbGw9IiM4MDdhNmQiIGQ9Ik0xMyA3aDF2MWgtMXoiLz48cGF0aCBmaWxsPSIjMzgzNDJhIiBkPSJNOSA0djFoMnYyaDFWNmgtMlY0eiIvPjwvc3ZnPgo=)](https://fabricmc.net/)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-orange?logo=kotlin&logocolor=white)](https://kotlinlang.org/)
  [![Java](https://img.shields.io/badge/Java-jdk%2021-red?logo=openjdk&logocolor=white)](https://jdk.java.net/21/)
  
  ${{\color{red}{\textsf{ This\ mod\ is\ currently\ in\ pre-alpha\ and\ is\ in\ active\ development\ -}}}}\$
  ${{\color{red}{\textsf{many\ features\ of\ this\ mod\ have\ not\ been\ fully\ implemented \}}}}\$
</div>

## Overview

Datapack-IDE is a minecraft mod that allows in game data pack editing with a built in text editor.


The goal of this mod aside from basic coding functionality expected from vscode is:
* 1: to provide tools ingame for debugging or fast implementation to speed up datapack development
* 2: to provide a collaborative datapack programming environment.

| GUI | Menu
|:-:|:-:|
|![preview_1](https://github.com/user-attachments/assets/34fb01f6-20d0-4813-8b87-81d70ad0a3dd)|![preview_2](https://github.com/user-attachments/assets/421dee13-82b1-4578-953c-1219a8e3b215)|


## What's implemented?

* A working GUI menu and basic (barebones) code editor
* themes (not usable for user application)
* file tree

## Future plans
* support for [spyglassMC](https://github.com/SpyglassMC/Spyglass)
* multiplayer support
* support for [beet](https://github.com/mcbeet/beet)

## Libraries Used

Datapack IDE relies on the following software:

| Software                                                                      | License                                                                                           |
|-------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| [GSON](https://github.com/google/gson)                                        | [Apache 2.0](https://github.com/google/gson/blob/master/LICENSE)                                  |
| [Fabric API](https://github.com/FabricMC/fabric)                              | [Apache 2.0](https://github.com/FabricMC/fabric/blob/1.21.6/LICENSE)                              |
| [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin/) | [Apache 2.0](https://github.com/FabricMC/fabric-language-kotlin/blob/master/LICENSE)              |
| [Fabric Loom](https://github.com/FabricMC/fabric-loom)                        | [MIT](https://github.com/FabricMC/fabric-loom/blob/dev/1.10/LICENSE)                              |
| [DevAuth](https://github.com/DJtheRedstoner/DevAuth)                          | [MIT](https://github.com/DJtheRedstoner/DevAuth/blob/master/LICENSE)                              |
| [JavaFX](https://openjfx.io/)                                                 | [GPL v2](https://openjdk.org/legal/gplv2+ce.html)                                                 |
