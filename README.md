# OnlineManager  
**Моніторинг активних DHCP-сесій Juniper MX (Radius)**

Онлайн-інструмент для NOC-інженерів, який показує в реальному часі, хто зараз підключений до мережі через DHCP на Juniper MX-ах (через таблицю `radacct`) за технологією Broadband Subscriber Management.

### Основні можливості

- Таблиця всіх активних сесій з фільтрами:
  - за днями (скільки днів назад дивитися)
  - онлайн/всі сесії
  - за username або Framed-IP
  - за ім’ям клієнта / локацією
- Перегляд дублікатів сесій (коли один username має кілька активних сесій)
- Корекція часу зупинки сесії (для боротьби з "завислими" сесіями)
- Налаштування підключення до бази Radius (MySQL)

### Для кого це

- NOC-інженери, які обслуговують Juniper MX з Broadband Subscriber Management
- Люди, які щодня дивляться в radacct, щоб зрозуміти, хто зараз "висить" у мережі
- Ті, кому набридло писати запити руками в HeidiSQL / MySQL Workbench

### Як запустити

#### Windows (рекомендовано)
1. Завантаж останній реліз → `Onlinemanager-x.x.xxxxx.exe`
2. Розпакуй (або просто запусти)
3. Запусти exe

#### Linux (Ubuntu / Debian)
1. Завантаж останній реліз → `onlinemanager_x.x.xxxxx_amd64.deb`
2. Встанови:
   ```bash
   sudo dpkg -i onlinemanager_*.deb
   ```
3. Запусти:
   ```bash
   /opt/onlinemanager/bin/onlinemanager
   ```

## Вимоги
* Java 21 або вище
* Доступ до бази Radius (MySQL)
* Підтримувані ОС: Windows 10/11, Debian 11 та вище, Ubuntu 20.04+ (зборка deb в Actions)

## Ліцензія

Apache License 2.0. Див. [LICENSE](LICENSE)

### Автор
Oleksandr Russkikh /aka Olden Gremlin &lt;olden@ukr-com.net&gt;
