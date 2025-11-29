#!/bin/bash
set -e

THREADS=${1:-8}
N=${2:-12}
THRESHOLD=${3:-2}

echo "Compilando NQueensForkJoin..."
javac -d out src/nqueens/NQueensForkJoin.java

echo "Ejecutando NQueensForkJoin con $THREADS threads, N=$N, threshold=$THRESHOLD"
echo "--------------------------------"
java -cp out nqueens.NQueensForkJoin $THREADS $N $THRESHOLD
echo "--------------------------------"
echo "Ejecución finalizada."
