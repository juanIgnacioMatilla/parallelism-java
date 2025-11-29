# Actividad Java
Implementaciones secuenciales y paralelas para multiplicación de matrices y N-Queens.

## Hardware y entorno  
**CPU:** AMD Ryzen AI 9 365 — 10 núcleos / 20 hilos — ~1996 MHz  
**SO:** Ubuntu 22.04 (WSL2)  
**Java:** OpenJDK 21

## Punto a - Versión secuencial  
**Archivo:** `src/matrix/MatrixSequential.java`

### Puntos importantes  
- Implementa la multiplicación de matrices 1024×1024 usando triple bucle como en el ejemplo de la consigna.  
- Las matrices se inicializan de forma determinística con `Random(SEED)`.  
- Imprime `C[0][0]` como verificación del resultado y el tiempo de ejecución.

### Cómo ejecutarlo  
Desde el directorio del proyecto:

```bash
./scripts/run_matrix_sequential.sh
```

### Salida esperada

```
Compilando MatrixSequential...
Ejecutando MatrixSequential...
--------------------------------
Fin: 253.90358035476618
Tiempo (ms): 1991.526451
--------------------------------
Ejecución finalizada.
```

## Punto b - Versión paralela con ExecutorService  
**Archivo:** `src/matrix/MatrixExecutor.java`

### Puntos importantes  
- Paraleliza el cálculo por **filas**, enviando cada una como tarea al `ExecutorService` con la funcion `executor.submit`.  
  **Justificación:** Se paraleliza por filas porque cada una se puede calcular de manera completamente independiente y permite generar una cantidad razonable de tareas (una por fila). Paralelizar por columnas también sería igualmente válido. Paralelizar por celdas produciría un número excesivo de tareas muy pequeñas, y paralelizar el bucle interno `k` no tiene sentido conceptual, ya que ese bucle calcula un único valor `C[i][j]`.
 
- Permite configurar la cantidad de hilos desde línea de comandos (primer argumento).  
- Produce el mismo resultado que la versión secuencial y muestra el tiempo de ejecución y la cantidad de hilos utilizados.

### Cómo ejecutarlo  
Con cantidad de hilos específica (ejemplo: 8):

```bash
./scripts/run_matrix_executor.sh 8
```

O usando el valor por defecto (todos los cores disponibles):

```bash
./scripts/run_matrix_executor.sh
```

### Salida esperada
```
Compilando MatrixExecutor...
Ejecutando MatrixExecutor...
--------------------------------
Fin: 253.90358035476618
Tiempo (ms): 366.863845
Threads usados: 8
--------------------------------
Ejecución finalizada.
```

## Punto c - Versión paralela con ForkJoin  
**Archivo:** `src/matrix/MatrixForkJoin.java`

### Puntos importantes  
- Implementa paralelismo usando el framework **ForkJoin**, dividiendo recursivamente el rango de filas en subtareas hasta alcanzar un `threshold` configurable.  
- Permite ajustar tanto la cantidad de hilos como el `threshold` desde la línea de comandos.  
- Produce el mismo resultado que las versiones anteriores y muestra el tiempo de ejecución, los hilos utilizados y el threshold aplicado.

### Cómo ejecutarlo  
Con cantidad de hilos y threshold específicos (ejemplo: 8 hilos, threshold 64):

```bash
./scripts/run_matrix_forkjoin.sh 8 64
```

Usando valores por defecto (todos los cores disponibles y threshold = 64):

```bash
./scripts/run_matrix_forkjoin.sh
```

### Salida esperada

```
Compilando MatrixForkJoin...
Ejecutando MatrixForkJoin...
--------------------------------
Fin: 253.90358035476618
Tiempo (ms): 473.427032
Threads usados: 8
Threshold: 64
--------------------------------
Ejecución finalizada.
```

## Punto e - Versión paralela con Virtual Threads  
**Archivo:** `src/matrix/MatrixVirtual.java`

### Puntos importantes  
- Reimplementa la versión del punto **b)** reemplazando el `ExecutorService` por **virtual threads** utilizando `Thread.startVirtualThread`.  
- Mantiene la misma estrategia de paralelización por filas, pero permite crear cientos o miles de hilos sin costo alto, gracias a que los virtual threads son gestionados por la JVM y no por el sistema operativo.  
- Permite configurar la cantidad de virtual threads desde línea de comandos; si no se indica, se utiliza una virtual thread por fila (1024).  
- Produce el mismo resultado que las versiones anteriores e informa el tiempo de ejecución y la cantidad de hilos virtuales utilizados.

### Cómo ejecutarlo  
Con cantidad específica de virtual threads (ejemplo: 20):

```bash
./scripts/run_matrix_virtual.sh 20
```

Usando valor por defecto (1024 virtual threads, una por fila):

```bash
./scripts/run_matrix_virtual.sh
```

### Salida esperada

```
Compilando MatrixVirtual...
Ejecutando MatrixVirtual...
--------------------------------
Fin: 253.90358035476618
Tiempo (ms): 373.285158
Virtual threads usados: 1024
--------------------------------
Ejecución finalizada.
```

