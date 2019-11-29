#!/usr/bin/env bash
HOME_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd "${HOME_DIR}" || exit

cp plugin-test-util.jar "/Volumes/Pegasus/Data"
cp lib/flexmark-util.jar "/Volumes/Pegasus/Data"
cp lib/flexmark-tree-iteration.jar "/Volumes/Pegasus/Data"
echo updated plugin-test-util.jar in "/Volumes/Pegasus/Data"

cp plugin-test-util.jar ../MissingInActions/lib
cp lib/flexmark-test-util.jar ../MissingInActions/lib
echo updated plugin-test-util.jar in ../MissingInActions/lib

cp plugin-test-util.jar ../touch-typists-completion-caddy/lib
cp lib/flexmark-test-util.jar ../touch-typists-completion-caddy/lib
echo updated plugin-test-util.jar in ../touch-typists-completion-caddy/lib

cp plugin-test-util.jar ../CLionArduinoPlugin/lib
cp lib/flexmark-test-util.jar ../CLionArduinoPlugin/lib
echo updated plugin-test-util.jar in ../CLionArduinoPlugin/lib

cp plugin-test-util.jar ../idea-multimarkdown3/lib
cp lib/flexmark-test-util.jar ../idea-multimarkdown3/lib
echo updated plugin-test-util.jar in ../idea-multimarkdown3/lib

#cp plugin-test-util.jar ../idea-multimarkdown2/lib
#cp lib/flexmark-util.jar ../idea-multimarkdown2/lib
#echo updated plugin-test-util.jar in ../idea-multimarkdown2/lib
#
#cp plugin-test-util.jar ../idea-multimarkdown1/lib
#cp lib/flexmark-util.jar ../idea-multimarkdown1/lib
#echo updated plugin-test-util.jar in ../idea-multimarkdown1/lib
