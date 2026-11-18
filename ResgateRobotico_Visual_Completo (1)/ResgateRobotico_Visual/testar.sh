#!/bin/sh
set -e
cd "$(dirname "$0")"
sh compilar.sh
java -cp bin TestesSistema
java -cp bin TesteRetomada gravar
java -cp bin TesteRetomada retomar
