#!/usr/bin/env bash
#
#   Copyright 2019 yesday.github.io
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

if [ -z "$YDSU_HOME" ]; then
    YDSU_HOME="$HOME/appdata/ydsu"
fi
YDSU_CACHE="${YDSU_HOME}/cache"
YDSU_MODULE="${YDSU_HOME}/src/main/groovy/ydsu/module"

# Local variables
ydsu_repo=https://github.com/yesday/ydsu.git

echo ''
echo '              _'
echo '             | |'
echo '  _   _    __| |  ___   _   _' 
echo ' | | | |  / _` | / __| | | | |'
echo ' | |_| | | (_| | \__ \ | |_| |'
echo '  \__, |  \__,_| |___/  \__,_|'
echo '   __/ |'
echo '  |___/'
echo ''
echo ''
echo '                                                                 Now attempting installation...'
echo ''
echo ''

echo "Looking for a previous installation of YDSU..."
if [ -d "$YDSU_HOME" ]; then
	echo "YDSU found."
	echo ""
	echo "======================================================================================================"
	echo " You already have YDSU installed."
	echo " YDSU was found at:"
	echo ""
	echo "    ${YDSU_HOME}"
	echo ""
	echo " Please consider running the following if you need to upgrade."
	echo ""
	echo "    $ ydsuPull"
	echo ""
	echo " Alternatively, to reset the existing installation perform the following steps."
	echo ""
	echo "    $ ydsuUninstall"
	echo "    Manually delete ${YDSU_HOME}"
	echo "    Run this script again"
	echo ""
	echo "======================================================================================================"
	echo ""
	exit 0
fi

echo "Looking for git..."
if [ -z $(which git) ]; then
	echo "Not found."
	echo "======================================================================================================"
	echo " Please install git on your system using your favourite package manager."
	echo ""
	echo " Restart after installing git."
	echo "======================================================================================================"
	echo ""
	exit 1
fi

echo "Looking for groovy..."
if [ -z $(which groovy) ]; then
	echo "Not found."
	echo "======================================================================================================"
	echo " Please install groovy on your system using your favourite package manager."
	echo ""
	echo " Restart after installing groovy."
	echo "======================================================================================================"
	echo ""
	exit 1
fi

echo "Looking for ansible..."
if [ -z $(which ansible) ]; then
	echo "Not found."
	echo "======================================================================================================"
	echo " Please install ansible on your system using your favourite package manager."
	echo ""
	echo " Restart after installing ansible."
	echo "======================================================================================================"
	echo ""
	exit 1
fi

echo "Installing YDSU with default distribution..."

# Create directory structure

echo "Create distribution directories..."
# The following `cd; cd -` prevents the error caused by (optionally) re-setting the ydsu directory:
# shell-init: error retrieving current directory: getcwd: cannot access parent directories: No such file or directory
cd; cd -
mkdir -p "${YDSU_CACHE}/yesday"
git clone ${ydsu_repo} "${YDSU_CACHE}/yesday/ydsu"
cp -R "${YDSU_CACHE}/yesday/ydsu/src" "${YDSU_HOME}"
cp -R "${YDSU_CACHE}/yesday/ydsu/src/main/resources/distribution" "$YDSU_HOME"
cp -R "${YDSU_CACHE}/yesday/ydsu/src/main/resources/version.txt" "$YDSU_HOME"

PATH="$PATH:\
${YDSU_MODULE}/manage:\
${YDSU_MODULE}/util"

"${YDSU_MODULE}/manage/ydsuLoadModules"

echo -e "\n\n\nAll done!\n\n"

echo "Please open a new terminal, or run the following in the existing one:"
echo ""
echo "    source ~/.bashrc"
echo ""
