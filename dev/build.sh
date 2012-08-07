#!/bin/sh
# when using xvnc plug-in in jenkins results in error (if owner of .vnc and .xauthority are builders (tomcat, bitnami) otherwise the 
# 'wrong type or access mode for /home/bitnami/.vnc' error is shown): 
#xauth:  timeout in locking authority file /home/bitnami/.Xauthority
#New 'ip-10-252-138-103:26 (root)' desktop is ip-10-252-138-103:26
#Starting applications specified in /home/bitnami/.vnc/xstartup
#Log file is /home/bitnami/.vnc/ip-10-252-138-103:26.log

# when using manually installed vnc4server with the export DISPLAY=$DISPLAY command, junit tests crash at any point with:
# testCreateTreeFromFileWithLeadingSeparator(ch.hsr.ifs.cdt.metriculator.model.test.TreeBuilderTest)  Time elapsed: 0.122 sec  <<< ERROR!
# java.lang.NoClassDefFoundError: org/eclipse/ui/wizards/datatransfer/IImportStructureProvider
# ... ... ... Caused by: org.eclipse.swt.SWTError: No more handles [gtk_init_check() failed] at org.eclipse.swt.SWT.error(SWT.java:4308)
# surprisingly env shows DISPLAY= (empty)
export PATH=/usr/local/apache-maven-3.0.4/bin:$PATH
export DISPLAY=:12
THIS=$(readlink -f $0)
BUNDLE_ROOT="`dirname $THIS`"
env
echo $HOME
cd $BUNDLE_ROOT
mvn -e -Dmaven.repo.local=/var/m2 clean install
