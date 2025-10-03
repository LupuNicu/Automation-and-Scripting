# IW01: Scrierea unui scenariu unic de obuz pentru automatizarea sarcinilor

## Descriere:

Acest script șterge fișierele dintr-un director specificat care au o anumită extensie (sau extensii multiple).  
Dacă nu sunt specificate extensii, va șterge fișierele cu extensia `tmp` în mod implicit.

Este util pentru curățarea automată a fișierelor temporare sau inutile dintr-un director.

---

## Utilizare

    ./delete_by_extension.sh <director> [extensie1 extensie2 ...]

### Parametri

* <director>: Directorul în care se vor căuta fișierele.

* [extensie1 extensie2 ...]: (Opțional) Lista de extensii ale fișierelor ce trebuie șterse (fără punct, ex: log, tmp, bak).

## Comportament

* Dacă nu se oferă extensii, scriptul va șterge fișierele cu extensia .tmp.

* Scriptul verifică dacă directorul există. Dacă nu, se oprește cu mesaj de eroare.

* Fișierele sunt căutate recursiv în directorul dat.

* Numărul total de fișiere șterse este afișat la final.

## Exemple

Ștergere implicită (.tmp) în directorul /home/user/proiect:

    ./delete_by_extension.sh /home/user/proiect

Ștergere fișiere .log și .bak din directorul curent:

    ./delete_by_extension.sh . log bak

Eroare dacă directorul nu există:

    ./delete_by_extension.sh /cale/inexistenta txt

Output: Eroare: Directorul '/cale/inexistenta' nu exista!

### Output așteptat:
Total fisiere sterse: 12
