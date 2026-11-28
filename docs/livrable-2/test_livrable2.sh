#!/usr/bin/env bash
# Recette automatique du livrable 2 (PlayerService + GameService)
# A lancer pendant que "docker compose up" tourne :  bash test_livrable2.sh
# Chaque test affiche OK ou KO, avec le code HTTP attendu et obtenu.

PS=http://localhost:8081
GS=http://localhost:8082
RUN=$(date +%s)          # suffixe pour avoir des emails uniques a chaque lancement
PASS=0; FAIL=0
BODY=/tmp/skull_body.json

# --- outils ---------------------------------------------------------------
req() {  # req METHODE URL [JSON] [en-tetes...]  -> affiche le code HTTP, corps dans $BODY
  local m=$1 url=$2 data=$3; shift 3
  local args=(-s -o "$BODY" -w '%{http_code}' -X "$m" -H 'Content-Type: application/json')
  for h in "$@"; do args+=(-H "$h"); done
  [ -n "$data" ] && args+=(-d "$data")
  curl "${args[@]}" "$url"
}
json() { python3 -c "import json,sys; d=json.load(open('$BODY')); print($1)" 2>/dev/null; }
check() {  # check ID "description" attendu obtenu
  if [ "$3" == "$4" ]; then echo "OK  $1  $2 (attendu $3, obtenu $4)"; PASS=$((PASS+1))
  else echo "KO  $1  $2 (attendu $3, obtenu $4)"; FAIL=$((FAIL+1)); fi
}

echo "=== Recette livrable 2 - $(date '+%d/%m/%Y %H:%M') ==="

# --- PlayerService ----------------------------------------------------------
E1="joueur1_$RUN@skull.fr"; E2="joueur2_$RUN@skull.fr"; E3="joueur3_$RUN@skull.fr"

c=$(req POST $PS/players "{\"email\":\"$E1\",\"username\":\"J1_$RUN\",\"password\":\"secret123\"}")
check T05 "Inscription joueur1" 201 "$c"
req POST $PS/players "{\"email\":\"$E2\",\"username\":\"J2_$RUN\",\"password\":\"secret123\"}" >/dev/null
req POST $PS/players "{\"email\":\"$E3\",\"username\":\"J3_$RUN\",\"password\":\"secret123\"}" >/dev/null

c=$(req POST $PS/players/login "{\"email\":\"$E1\",\"password\":\"secret123\"}")
check T06 "Connexion joueur1" 200 "$c"
ID1=$(json "d['id']"); TOK=$(json "'oui' if d.get('token') else 'non'")
check T06b "Token JWT renvoye a la connexion" oui "$TOK"
req POST $PS/players/login "{\"email\":\"$E2\",\"password\":\"secret123\"}" >/dev/null; ID2=$(json "d['id']")
req POST $PS/players/login "{\"email\":\"$E3\",\"password\":\"secret123\"}" >/dev/null; ID3=$(json "d['id']")

c=$(req POST $PS/players/login "{\"email\":\"$E1\",\"password\":\"mauvais\"}")
check T07 "Connexion avec mauvais mot de passe" 401 "$c"

c=$(req POST $PS/players "{\"email\":\"$E1\",\"username\":\"autre\",\"password\":\"secret123\"}")
check T08 "Inscription avec email deja utilise" 409 "$c"

c=$(req POST $PS/players "{\"email\":\"court_$RUN@skull.fr\",\"username\":\"court\",\"password\":\"abc\"}")
check T09 "Inscription avec mot de passe trop court" 400 "$c"

c=$(req POST $PS/players/login '{"email":"admin@skull.fr","password":"admin123"}')
check T04 "Connexion admin" 200 "$c"
ADMIN=$(json "d['id']"); ISADM=$(json "str(d['admin']).lower()")
check T03 "Le compte admin a bien le role admin" true "$ISADM"

c=$(req POST $PS/reports "{\"reporterId\":\"$ID1\",\"reportedId\":\"$ID2\",\"gameId\":null,\"reason\":\"Suspicion de triche\"}")
check T11 "Signalement de joueur2 par joueur1" 201 "$c"
req GET $PS/reports "" >/dev/null
VU=$(json "'oui' if any(r['reportedId']=='$ID2' for r in d) else 'non'")
check T11b "Signalement visible dans la liste admin" oui "$VU"

c=$(req PUT $PS/players/$ID2/ban "" "X-User-Id: $ID1")
check T14 "Bannissement par un joueur non admin refuse" 403 "$c"

c=$(req PUT $PS/players/$ID2/ban "" "X-User-Id: $ADMIN")
check T12 "Bannissement de joueur2 par l'admin" 200 "$c"
c=$(req POST $PS/players/login "{\"email\":\"$E2\",\"password\":\"secret123\"}")
check T12b "Joueur banni ne peut plus se connecter" 403 "$c"

c=$(req PUT $PS/players/$ID2/unban "" "X-User-Id: $ADMIN")
check T13 "Debannissement par l'admin" 200 "$c"
c=$(req POST $PS/players/login "{\"email\":\"$E2\",\"password\":\"secret123\"}")
check T13b "Joueur debanni peut se reconnecter" 200 "$c"

# --- GameService ------------------------------------------------------------
H1=("X-User-Id: $ID1" "X-Username: J1_$RUN")
H2=("X-User-Id: $ID2" "X-Username: J2_$RUN")
H3=("X-User-Id: $ID3" "X-Username: J3_$RUN")

c=$(req POST $GS/games '{"maxPlayers":4,"aiCount":1}' "${H1[@]}")
check T15 "Creation partie 4 joueurs dont 1 IA" 201 "$c"
G=$(json "d['id']"); HJ=$(json "d['humansJoined']")
check T15b "Le createur est inscrit dans sa partie" 1 "$HJ"
req GET $GS/games "" >/dev/null
VU=$(json "'oui' if any(g['id']=='$G' for g in d) else 'non'")
check T15c "La partie apparait dans les parties ouvertes" oui "$VU"

c=$(req POST $GS/games '{"maxPlayers":2,"aiCount":0}' "${H1[@]}"); check T16 "Partie a 2 joueurs refusee" 400 "$c"
c=$(req POST $GS/games '{"maxPlayers":7,"aiCount":0}' "${H1[@]}"); check T16b "Partie a 7 joueurs refusee" 400 "$c"
c=$(req POST $GS/games '{"maxPlayers":4,"aiCount":4}' "${H1[@]}"); check T17 "Trop d'IA refuse" 400 "$c"

c=$(req POST $GS/games/$G/join '{}' "${H2[@]}")
check T18 "Joueur2 rejoint la partie" 200 "$c"
check T18b "Nombre d'humains passe a 2" 2 "$(json "d['humansJoined']")"

c=$(req POST $GS/games/$G/join '{}' "${H2[@]}")
check T19 "Rejoindre deux fois refuse" 409 "$c"

c=$(req POST $GS/games/$G/leave '' "${H2[@]}")
check T21 "Joueur2 quitte la partie en attente" 204 "$c"
req GET $GS/games/$G "" >/dev/null
check T21b "Joueur2 retire du salon" 1 "$(json "d['humansJoined']")"

req POST $GS/games/$G/join '{}' "${H2[@]}" >/dev/null
c=$(req POST $GS/games/$G/leave '' "${H1[@]}")
check T22 "Le createur quitte la partie" 204 "$c"
req GET $GS/games/$G "" >/dev/null
check T22b "Joueur2 devient createur" "$ID2" "$(json "d['creatorId']")"
req POST $GS/games/$G/leave '' "${H2[@]}" >/dev/null
c=$(req GET $GS/games/$G "")
check T22c "Partie supprimee quand plus aucun humain" 404 "$c"

req POST $GS/games '{"maxPlayers":3,"aiCount":0}' "${H1[@]}" >/dev/null; G2=$(json "d['id']")
req POST $GS/games/$G2/join '{}' "${H2[@]}" >/dev/null
req POST $GS/games/$G2/join '{}' "${H3[@]}" >/dev/null
check T20 "Partie pleine -> lancee automatiquement" IN_PROGRESS "$(json "d['status']")"
req GET $GS/games "" >/dev/null
VU=$(json "'non' if any(g['id']=='$G2' for g in d) else 'oui'")
check T20b "La partie lancee n'est plus dans la liste" oui "$VU"

# --- Anomalies ----------------------------------------------------------------
req GET $PS/players/$ID1/dashboard "" >/dev/null
DASH=$(python3 -c "print('oui' if 'refused' in open('$BODY').read() else 'non')")
check T23 "Dashboard player-service sans 'Connection refused'" non "$DASH"

c=$(req PUT $PS/players/$ID3/ban "" "X-User-Id: $ADMIN")   # sans aucun token
check A2 "Ban accepte sans token, avec un X-User-Id falsifie (faille)" 401 "$c"
req PUT $PS/players/$ID3/unban "" "X-User-Id: $ADMIN" >/dev/null

echo "=== Resultat : $PASS OK, $FAIL KO ==="
