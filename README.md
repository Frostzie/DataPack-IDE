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

## 📝 Overview

Datapack-IDE is a minecraft mod that allows in game data pack editing with a built in text editor.

| GUI | Menu
|:-:|:-:|
|![preview_1](https://github.com/user-attachments/assets/34fb01f6-20d0-4813-8b87-81d70ad0a3dd)|![preview_2](https://github.com/user-attachments/assets/421dee13-82b1-4578-953c-1219a8e3b215)|

The goal of this mod aside from basic coding functionality expected from vscode is:
* 1: to provide tools ingame for debugging or fast implementation to speed up datapack development
* 2: to provide a collaborative datapack programming environment.

## ✅ What's implemented?

* saving and editing
* A working GUI menu and basic (barebones) code editor
* themes (not usable for user application)
* file tree

## 👷 Future plans
* support for [spyglassMC](https://github.com/SpyglassMC/Spyglass)
* multiplayer support
* support for [beet](https://github.com/mcbeet/beet)

## 📚 Libraries Used

Datapack IDE relies on the following Libraries:

| Software                                                                      | License                                                                                           |
|-------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------|
| [GSON](https://github.com/google/gson)                                        | [Apache 2.0](https://github.com/google/gson/blob/master/LICENSE)                                  |
| [Fabric API](https://github.com/FabricMC/fabric)                              | [Apache 2.0](https://github.com/FabricMC/fabric/blob/1.21.6/LICENSE)                              |
| [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin/) | [Apache 2.0](https://github.com/FabricMC/fabric-language-kotlin/blob/master/LICENSE)              |
| [Fabric Loom](https://github.com/FabricMC/fabric-loom)                        | [MIT](https://github.com/FabricMC/fabric-loom/blob/dev/1.10/LICENSE)                              |
| [DevAuth](https://github.com/DJtheRedstoner/DevAuth)                          | [MIT](https://github.com/DJtheRedstoner/DevAuth/blob/master/LICENSE)                              |
| [JavaFX](https://openjfx.io/)                                                 | [GPL v2](https://openjdk.org/legal/gplv2+ce.html)                                                 |

<br>

## 🧪 Want to test out the mod?
<details>
<summary><b>click to view process to get experimental build for play testing</b></summary>
<hr>
go to Actions -> go to the latest workflow shown on top
<img width="667" height="343" alt="image" src="https://github.com/user-attachments/assets/cec35fa7-c6ec-46b4-8ac1-407a5b29733e" />

Then download Artifacts.
Unzip Artifact folder upon download and drag the jar file into your mod folder for minecraft 1.21.8, make sure to install the appropriate dependencies, such as kotlin API and fabric API. Then you should be good to go

Warning that this version isn't a stable build as it is a dev build, if the current artifact build is having issues, feel free to file a issue report.<br>
If you are only interested in running a build for your own use, try older artifacts if the lastest isn't working.
</details>
<hr>
## 👥 Contributers

|Profile|Name|Role/Involvement|what I'm here for|
|-|-|-|-|
|<img src="https://github.com/Frostzie.png" width="60px;"/> | [Frostzie](https://github.com/Frostzie)| Lead developer                             | Second big project, first being skyfall - a skyblock utility mod. The heavy lifter here|
|<img src="https://github.com/Arttale.png" width="60px;"/>  | [Arttale](https://github.com/Arttale)  | Designer, Assistant developer, Datapack dev| Mostly here for consulting and project direction, help shaped the look and direction of the mod and this readme, also good looking|
|<img src="https://github.com/CrazyH2.png" width="60px;"/>  | [Huckle](https://github.com/CrazyH2)   | Web developer, Datapack dev                | Great help with CSS and HTML side of things|

## 🖐️ Want to support the project?
Here's a donation link:<br>
[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/gbraad)
<br>
All proceeds goes to frostzie

## ☝️ Want to give feedback or request features?
<hr>
Any input is appreciated here
<h4 align="left">
  <a href="https://github.com/Frostzie/DataPack-IDE/issues/new?title=Feedback%3A+&labels=feedback%2C&assignees=Frostzie%2C">🗒️ Open Feedback Issue</a>
<br>
<br>
  <a href="https://github.com/Frostzie/DataPack-IDE/issues/new?title=Feature%20Request%3A+&labels=featurerequest%2C&assignees=Frostzie%2C">💡 Open Feature Issue</a>
</h4>
<hr>
