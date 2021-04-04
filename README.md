# HealthCare
*A **neat** serverside mod that shows healthbars above mobs' names.*

[![GitHub license](https://img.shields.io/github/license/samolego/HealthCare?style=flat-square)](https://github.com/samolego/HealthCare/blob/master/LICENSE)
[![Server environment](https://img.shields.io/badge/Environment-server-blue?style=flat-square)](https://github.com/samolego/HealthCare)
[![Singleplayer environment](https://img.shields.io/badge/Environment-singleplayer-yellow?style=flat-square)](https://github.com/samolego/HealthCare)

Features custom health icons, customisable length etc. (all **per-player**)

<img src="https://user-images.githubusercontent.com/34912839/113518367-30b11500-9586-11eb-8907-9af0e5bcb255.png" width="300px">
<img src="https://user-images.githubusercontent.com/34912839/113518381-47f00280-9586-11eb-91ee-3ac1130507f9.png" width="300px">

## Commands & permissions

### Player
*(all granted by default)*
* `/healthbar toggle` - toggles healthbar (`healthcare.healthbar.toggle`)
* `/healthbar edit style <STYLE>` - edits healthbar style (`healthcare.healthbar.edit.style`)
* `/healthbar edit alwaysVisible <BOOLEAN>` - sets whether healthbar is visible just on entity hover or always (`healthcare.healthbar.edit.visibility`)
* `/healthbar custom healthbarLength <INT>` - changes length of healthbar (`healthcare.healthbar.edit.custom.length`)
* `/healthbar custom fullSymbol <CHAR>` - sets custom "full-heart" symbol (`healthcare.healthbar.edit.symbol.full`)
* `/healthbar custom emptySymbol <CHAR>` - sets custom "empty-heart" symbol (`healthcare.healthbar.edit.symbol.empty`)


### Admin
* `/healthcare reloadConfig` - reloads config (`healthcare.reloadConfig`)