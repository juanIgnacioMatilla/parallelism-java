#!/bin/bash

set -e

N=${1:-12}

echo "Compilando NQueensSequential..."
javac -d out src/nqueens/NQueensSequential.java

echo "Ejecutando NQueensSequential con N=$N..."
echo "--------------------------------"
java -cp out nqueens.NQueensSequential $N
echo "--------------------------------"
echo "Ejecución finalizada."
