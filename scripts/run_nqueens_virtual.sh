#!/bin/bash
set -e

N=${1:-12}
TH=${2:-2}

echo "Compilando NQueensVirtual..."
javac -d out src/nqueens/NQueensVirtual.java

echo "Ejecutando NQueensVirtual con N=$N, threshold=$TH"
echo "--------------------------------"
java -cp out nqueens.NQueensVirtual $N $TH
echo "--------------------------------"
echo "Ejecución finalizada."
