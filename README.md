<div align="center">

# ⚔️ HOLLOW KNIGHT — Java Edition

### A hand-built 2D Metroidvania — Java · LibGDX · Tiled · Real AI

**The Forgotten Crossroads are silent. Something ancient stirs. Descend.**

<br>

[![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#)
[![LibGDX](https://img.shields.io/badge/Engine-LibGDX-E74C3C?style=for-the-badge&logo=libgdx&logoColor=white)](#)
[![Tiled](https://img.shields.io/badge/Maps-Tiled-4F8FBA?style=for-the-badge)](#)
[![Architecture](https://img.shields.io/badge/Architecture-MVC-9B59B6?style=for-the-badge)](#)
[![License](https://img.shields.io/badge/License-Educational-blue?style=for-the-badge)](#)

[![Stars](https://img.shields.io/github/stars/Hichcas/MiniHollowknight?style=for-the-badge&color=yellow)](../../stargazers)
[![Forks](https://img.shields.io/github/forks/Hichcas/MiniHollowknight?style=for-the-badge&color=blue)](../../network/members)
[![Last Commit](https://img.shields.io/github/last-commit/Hichcas/MiniHollowknight?style=for-the-badge&color=orange)](../../commits)

<br>

**[▶ Watch the Demo](./video.mp4)** · **[⬇ Download Latest Release](../../releases/latest)** · **[🐞 Report a Bug](../../issues)**

<br>

> *"No cost too great."*

</div>

---

## 📖 Table of Contents

- [About](#-about-the-project)
- [Feature Showcase](#-feature-showcase)
- [The False Knight — Boss Deep Dive](#-boss-fight-false-knight)
- [Bestiary](#%EF%B8%8F-bestiary)
- [Architecture](#%EF%B8%8F-architecture)
- [Controls](#-controls)
- [Getting Started](#-getting-started)
- [Tech Stack](#-tech-stack)
- [Roadmap](#%EF%B8%8F-roadmap)
- [Credits](#-credits)

---

## 🕸️ About The Project

This is a **complete, from-scratch reimplementation** of Hollow Knight's core gameplay loop — not a tutorial clone, not an asset flip. Every system was engineered by hand for this project:

- 🎮 Physics-based platforming with pogo-bouncing, wall-slides, and momentum-driven dashing
- 🧠 Enemies that patrol, ambush, and hunt using real line-of-sight and state-machine AI
- ⚡ A boss that **makes decisions** — weighing distance, randomizing patterns, and refusing to repeat itself
- 💾 A full save/load pipeline serialized to JSON
- 🔮 A soul-and-charm build system that changes how you play

Built solo for the Advanced Programming course at **Sharif University of Technology**, and submitted to the **"Knights' Competition" (رقابت شوالیه‌ها)** graphics showcase.

<div align="center">
<img src="https://img.shields.io/badge/⏱️-Built_in_one_semester-lightgrey?style=flat-square" />
<img src="https://img.shields.io/badge/📐-90+_Java_classes-lightgrey?style=flat-square" />
<img src="https://img.shields.io/badge/🗺️-Hand--crafted_Tiled_maps-lightgrey?style=flat-square" />
</div>

---

## 🎮 Feature Showcase

<table>
<tr>
<td width="50%" valign="top">

### 🧭 Menus & Meta
- Main Menu → Start / Settings / Guide / Achievements
- **4 save slots**, full JSON persistence
- Live achievement pop-up notifier
- Remappable keybinds, volume & SFX control

</td>
<td width="50%" valign="top">

### 🗺️ World & Movement
- Hand-built **Tiled** maps: Forgotten Crossroads, Greenpath
- Variable-height jump, air dash, double jump
- Wall-slide + the signature **Pogo bounce**
- Hazard tiles with checkpoint respawn

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 👾 Enemy AI
- Patrol AI with ledge/wall detection (Mosscreep)
- Sight-cone hunters with committed charge attacks (Husk Hornhead)
- Flying pursuit AI (Winged Sentry)
- Stationary ranged sentinels with rage cycles (Crystal Guardian)

</td>
<td width="50%" valign="top">

### 🔮 Combat & Progression
- Soul-fueled spells: **Vengeful Spirit**, **Howling Wraiths**
- Discrete mask-based health + Focus healing
- **8 Charms** with a notch-limited loadout
- Full VFX layer: slashes, knockback, camera shake

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 🕵️ Secrets & NPCs
- **Zote** — a fully voiced NPC with branching, stateful dialogue and a scripted (harmless) rage attack
- **Cracked walls** — three-hit destructible secret entrances hiding a reward room

</td>
<td width="50%" valign="top">

### 🛠️ Dev & QA Tools
- Full **cheat console** (Ctrl + key): noclip, god mode, full heal, full soul, unlock all charms, kill all enemies, emergency heal, boss-arena teleport
- **EN / ES localization**, live-switchable from Settings
- Rebindable controls + a brightness slider

</td>
</tr>
</table>

---

## 🐉 Boss Fight: False Knight

<div align="center">

**The centerpiece of the project — a boss that actually thinks.**

</div>

```
IF   player_distance < CLOSE_RANGE        →  ↑ weight: Mace Slam
ELIF player_distance > FAR_RANGE          →  ↑ weight: Charge Run / Offensive Leap
                                           →  + random jitter (unpredictability)
                                           →  − repeat penalty (anti-spam)

ON   HP <= 50%   →  STUN → armor breaks → vulnerable core exposed
AFTER stun       →  Phase 2: faster movement, faster AI tick, new Slam Shockwave attack
```

| System | What makes it real |
|---|---|
| 🎯 **Distance-weighted AI** | Move probabilities shift live based on player proximity |
| 🎲 **Randomization layer** | No two fights play out the same way |
| 🚫 **Anti-spam guarantee** | The same move can never fire twice in a row |
| 💥 **5 unique attacks** | Mace Slam · Charge Run · Offensive Leap · Defensive Leap · Slam Shockwave |
| 🔓 **Stun & Phase 2** | Armor breaks at 50% HP, exposing a vulnerable core, then the fight speeds up |
| 🎥 **Arena lock + camera shake** | Gates seal on entry; every heavy hit rattles the screen |

---

## 🗡️ Bestiary

| Enemy | Type | AI Pattern |
|---|---|---|
| 🐛 Mosscreep | Ground patrol | Straight-line walk, reverses on walls & ledges |
| 🦅 Winged Sentry | Aerial pursuit | Tracks and closes in on the player once sighted |
| 👹 Husk Hornhead | Ground brute | Walk → rest → vision cone → committed charge |
| 🔷 Crystal Guardian | Ranged sentinel | Long-range laser → enraged charge → reset |
| 💬 Zote | NPC | Stateful branching dialogue + scripted rage attack |
| 🛡️ **False Knight** | **Boss Arena** | *(see above)* |

---

## 🏗️ Architecture

Strict **Model-View-Controller** separation — no god classes, no rendering logic in game state.

```
📦 project
├── Model/          → Pure game state — Knight, FalseKnight, enemies, charms, save data
│   └── Enums/       → State machines (KnightState, FalseKnightState, ZoteState, …)
├── View/            → Rendering, sprite animations, screens, VFX & shaders
│   └── Screen/       → MainMenu, HUD, Pause, Achievements, Guide, EndGame
├── Controller/      → Input, AI decision logic, physics, save/audio/achievement managers
└── io/…/lwjgl3/      → Desktop launcher entry point
```

Every actor in the game — enemy or boss — follows the same recipe:

```
State enum  →  drives  →  dedicated Controller  →  mutates  →  Model
                                                        ↓
                                              matching View/Animations renders it
```

This keeps AI, physics, and presentation completely decoupled — you can reskin any enemy without touching a single line of combat logic.

---

## 🎹 Controls

<div align="center">

| Move | Jump | Attack | Dash | Focus | Inventory | Pause |
|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| `←` `→` | `Z` | `X` | `C` | `A` | `I` | `Esc` |

*Fully remappable in Settings.*

<br>

**Cheat console** *(hold `Ctrl` +)*

| Key | Effect |
|:---:|---|
| `B` | Teleport to boss arena |
| `C` | Toggle noclip |
| `G` | Toggle god mode |
| `H` | Full heal |
| `S` | Refill soul |
| `U` | Unlock all charms |
| `K` | Kill all enemies on screen |
| `M` | Arm emergency heal |

</div>

---

## 🚀 Getting Started

### ▶️ Play instantly
Grab the latest runnable jar from **[Releases](../../releases)**:
```bash
java -jar HollowKnight.jar
```

### 🛠️ Build from source
```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPO.git
cd YOUR_REPO
./gradlew lwjgl3:run
```

---

## 🧩 Tech Stack

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![LibGDX](https://img.shields.io/badge/LibGDX-E74C3C?style=flat-square)
![Tiled](https://img.shields.io/badge/Tiled_Map_Editor-4F8FBA?style=flat-square)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)
![JSON](https://img.shields.io/badge/JSON_Persistence-000000?style=flat-square&logo=json&logoColor=white)

</div>

---

## 🗺️ Roadmap

- [x] Two hand-built biomes (Forgotten Crossroads, Greenpath)
- [x] Full boss fight with phase transition
- [x] Charm & Soul systems
- [x] JSON save/load
- [x] Secret rooms & destructible walls
- [x] EN / ES localization
- [ ] City of Tears & Crystal Peaks biomes
- [ ] Additional boss arena

---

## 🙏 Credits

Built for the **Advanced Programming** course — Department of Computer Engineering, **Sharif University of Technology**.
Instructor: Dr. Mohammadamin Fazli · Graphics Assignment Staff: Hamed Alinejad, Shahab Ahmadloo, Sepehr Kardel

Original game design, characters, and art direction © **Team Cherry**. This is a non-commercial, educational reimplementation built from scratch for coursework — not affiliated with or endorsed by Team Cherry.

<div align="center">

### If this project made you smile, consider dropping a ⭐

**Made with ❤️ and a lot of `Vector2` math.**

</div>
