#!/bin/bash

set -e

mkdir -p out

echo "Compilando MatrixVirtual..."
javac -d out src/matrix/MatrixVirtual.java

echo "Ejecutando MatrixVirtual..."
echo "--------------------------------"

if [ -z "$1" ]; then
    # sin argumentos
    java -cp out matrix.MatrixVirtual
else
    # con argumento
    java -cp out matrix.MatrixVirtual "$1"
fi

echo "--------------------------------"
echo "Ejecución finalizada."

