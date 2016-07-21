[FlowChat](http://flowchat.tk) &mdash; An open-source, live updating, threaded chat platform with voting. 
==========
![](http://img.shields.io/version/0.3.0.png?color=green)
[![Build Status](https://travis-ci.org/tchoulihan/flowchat.png)](https://travis-ci.org/tchoulihan/flowchat)

<!---
I made a live-updating, threaded discussion alternative to reddit and slack called FlowChat, written in java and angular2. Self-hostable, and open-source. 
-->

[FlowChat](http://flowchat.tk) is an open-source, self-hostable, **live-updating** discussion platform, featuring chatrooms with threaded conversations, and voting.

It can act as an alternative to forums, as a private team communication platform (like slack), a content creation platform (like reddit), or a voting/polling platform like [referendum](https://referendum.ml).

Flowchat tries to solve the problem of having a fluid, free-feeling group chat, while allowing for side conversations so that every comment isn't at the top level. Multiple conversations can take place at once, without interrupting the flow of the chatroom.

It uses [range voting](http://rangevoting.org/UniqBest.html) (also known as olympic score voting) for both comments and discussions. Range voting is more expressive than simple :thumbsup: or :thumbsdown: votes.

It features:
- A complete chat application with live updating, threaded discussion.
- Private or public discussions
- Customizable discussion and comment sorting, by recentness, and popularity.
- Discussion hashtags. 
- Discussion creators can block users, or delete comments.

Tech used:
- [Java Spark](https://github.com/perwendel/spark), [Bootstrap v4](https://github.com/twbs/bootstrap), [Angular2](https://github.com/angular/angular), [Angular-cli](https://github.com/angular/angular-cli), [ng2-bootstrap](http://valor-software.com/ng2-bootstrap/), [ActiveJDBC](http://javalite.io/activejdbc), [Liquibase](http://www.liquibase.org/), [Postgres](https://www.postgresql.org/), [Markdown-it](https://github.com/markdown-it/markdown-it), [angular2-toaster](https://github.com/Stabzs/Angular2-Toaster)

Check out a sample discussion [here](http://flowchat.tk/#/discussion/13).

Join the subreddit: [/r/flowchat](https://www.reddit.com/r/flowchat/)

[Change log](CHANGELOG.md)

----

## Screenshots:

<img src="http://i.imgur.com/lZBMsn5.png">

<img src="http://i.imgur.com/hwNc0mx.png">

<img src="http://i.imgur.com/JbOBf1h.png">

<img src="http://i.imgur.com/chhvZwC.png">

## Installation 

*If you want to self-host flowchat.*

### Requirements
- Java 8 + Maven
- Node + npm, [nvm](https://github.com/creationix/nvm) is the preferred installation method.
- Postgres 9.3 or higher

### Download Flowchat
`git clone https://github.com/tchoulihan/flowchat`

### Setup a postgres database
[Here](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-16-04) are some instructions to get your DB up and running.

### Edit your pom.xml file to point to your database
```sh
cd flowchat
vim service/pom.xml
```

Edit it to point to your own database:
```xml
<!--The Database location and login, here's a sample-->
<jdbc.url>jdbc:postgresql://127.0.0.1/flowchat</jdbc.url>
<jdbc.username>postgres</jdbc.username>
<jdbc.password></jdbc.password
<!--The sorting for discussions, comments, and tags are:
 	Sorting score =
		created_weight/(now_seconds - comment_seconds) +
		number_of_votes*number_of_votes_weight +
		avg_rank*avg_rank_weight-->
<sorting_created_weight>3600</sorting_created_weight>
<sorting_number_of_votes_weight>0.001</sorting_number_of_votes_weight>
<sorting_avg_rank_weight>0.01</sorting_avg_rank_weight>
```
### Install flowchat

for local testing: 

`./install.sh` and goto `http://localhost:4567/`

for a production environment, edit `ui/config/environment.prod.ts` to point to your hostname, then run:

`./install.sh -prod`

You can redirect ports in linux to route from port 80 to this port:

`sudo iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 4567`

## Bugs and feature requests
Have a bug or a feature request? If your issue isn't [already listed](https://github.com/tchoulihan/flowchat/issues/), then open a [new issue here](https://github.com/tchoulihan/flowchat/issues/new).
