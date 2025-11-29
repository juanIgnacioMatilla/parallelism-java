#!/bin/bash

set -e

THREADS=${1:-8}
N=${2:-12}

echo "Compilando NQueensExecutor..."
javac -d out src/nqueens/NQueensExecutor.java

echo "Ejecutando NQueensExecutor con $THREADS threads, N=$N..."
echo "--------------------------------"
java -cp out nqueens.NQueensExecutor $THREADS $N
echo "--------------------------------"
echo "Ejecución finalizada."
