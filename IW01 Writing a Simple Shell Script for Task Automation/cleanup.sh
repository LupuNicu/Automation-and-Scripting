# verificam daca s-a dat directorul
if [ $# -lt 1 ]; then
    echo "Utilizare: $0 <director> [extensie1 extensie2 ...]"
    exit 1
fi

DIR=$1
shift  # scoatem directorul din lista de argumente

# verificam daca directorul exista
if [ ! -d "$DIR" ]; then
    echo "Eroare: Directorul '$DIR' nu exista!"
    exit 1
fi

#folosim implicit "tmp"
if [ $# -eq 0 ]; then
    EXTS=("tmp")
else
    EXTS=("$@")
fi

COUNT=0

# pentru fiecare extensie stergem fisierele
for ext in "${EXTS[@]}"; do
    DELETED=$(find "$DIR" -type f -name "*.$ext" -delete -print | wc -l)
    COUNT=$((COUNT + DELETED))
done

echo "Total fisiere sterse: $COUNT"
