#!/bin/bash

# ============================================
# Ejecuta MatrixExecutor con una cantidad
# de hilos pasada como argumento.
# Si no se pasa argumento, usa el default.
# ============================================

set -e

mkdir -p out

echo "Compilando MatrixExecutor..."
javac -d out src/matrix/MatrixExecutor.java

echo "Ejecutando MatrixExecutor..."
echo "--------------------------------"
java -cp out matrix.MatrixExecutor "$1"
echo "--------------------------------"

echo "Ejecución finalizada."

