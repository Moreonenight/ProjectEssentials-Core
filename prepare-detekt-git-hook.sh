#!/bin/bash
file_location=.git/hooks/pre-commit

cat >$file_location <<EOF
#!/usr/bin/env bash
echo "Running detekt check..."
OUTPUT="/tmp/detekt-\$(date +%s)"
./gradlew detekt > \$OUTPUT
EXIT_CODE=\$?
if [ \$EXIT_CODE -ne 0 ]; then
  cat \$OUTPUT
  rm \$OUTPUT
  echo "***********************************************"
  echo "                 Detekt failed                 "
  echo " Please fix the above issues before committing "
  echo "***********************************************"
  exit \$EXIT_CODE
fi
rm \$OUTPUT
EOF
