#!/bin/bash

# ================================
#  Script para compilar y ejecutar
#  la versión secuencial de la
#  multiplicación de matrices
# ================================

set -e   # Si algo falla, abortar

# Crear carpeta de salida si no existe
mkdir -p out

echo "Compilando MatrixSequential..."
javac -d out src/matrix/MatrixSequential.java

echo "Ejecutando MatrixSequential..."
echo "--------------------------------"
java -cp out matrix.MatrixSequential
echo "--------------------------------"

echo "Ejecución finalizada."

