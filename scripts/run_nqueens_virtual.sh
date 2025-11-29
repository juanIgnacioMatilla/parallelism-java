#!/bin/bash
set -e

VT=${1:-200}      # cantidad de virtual threads que permitimos
N=${2:-12}
TH=${3:-2}

echo "Compilando NQueensVirtual..."
javac -d out src/nqueens/NQueensVirtual.java

echo "Ejecutando NQueensVirtual con $VT virtual threads, N=$N, threshold=$TH"
echo "--------------------------------"
java -cp out nqueens.NQueensVirtual $VT $N $TH
echo "--------------------------------"
echo "Ejecución finalizada."
