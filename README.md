# Dobri Visual (Fabric, Minecraft 1.21.11)

Клиентский мод: меню на Right Shift, FPS и координаты на экране.

## Как получить .jar без установки Java и Gradle (через GitHub)

1. Зарегистрируйся на https://github.com (бесплатно).
2. Нажми "+" -> "New repository", назови `dobri-visual`, нажми "Create repository".
3. Нажми "uploading an existing file", перетащи ВСЁ содержимое этой папки
   (включая скрытую папку `.github`, если не видно - включи показ скрытых файлов)
   и нажми "Commit changes".
4. Открой вкладку "Actions" -> "Build Dobri Visual" -> "Run workflow".
   Через 2-5 минут сборка станет зелёной.
5. Открой завершённый запуск, внизу в "Artifacts" скачай `Dobri-Visual-jar` - внутри будет `dobri-visual-1.0.0.jar`.

## Установка

1. Установи Fabric Loader для 1.21.11: https://fabricmc.net/use/installer/
2. Скачай Fabric API для 1.21.11 (Modrinth) и положи в `.minecraft/mods`.
3. Положи `dobri-visual-1.0.0.jar` в `.minecraft/mods`.
4. Запусти игру, в мире нажми Right Shift.

Настройки сохраняются в `.minecraft/config/dobri_visual.json`.

## Как добавить новую функцию

1. `Modules.java` - добавь `new Module(...)` и внеси в `LIST` (в меню появится переключатель).
2. `Hud.java` - добавь рисование, проверяя `Modules.ТВОЯ.enabled`.
