#!/bin/bash

set -e

mkdir -p out

echo "Compilando MatrixForkJoin..."
javac -d out src/matrix/MatrixForkJoin.java

echo "Ejecutando MatrixForkJoin..."
echo "--------------------------------"

# Manejo correcto de argumentos
if [ -z "$1" ]; then
    # Sin argumentos → usar defaults
    java -cp out matrix.MatrixForkJoin
elif [ -z "$2" ]; then
    # Solo 1 argumento → pasar numThreads
    java -cp out matrix.MatrixForkJoin "$1"
else
    # Dos argumentos → pasar numThreads y threshold
    java -cp out matrix.MatrixForkJoin "$1" "$2"
fi

echo "--------------------------------"
echo "Ejecución finalizada."

