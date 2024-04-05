FROM lscr.io/linuxserver/webtop:latest
RUN sudo apk add openjdk17
RUN sudo mkdir /score && cd /score && wget -O score.jar https://imac.org.au/Score/Score-4.53-Release.jar
RUN sudo wget -O /score/Score4.25.zip https://mini-iac.org/downloads/software/download-file?path=Score-4.25.2+2024+Sequences+V1.zip
RUN sudo unzip /score/Score4.25.zip -d /score
RUN sudo mv /score/Score-4.25.2\ 2024\ Sequences\ V1/* /score/
RUN sudo rm -rf /score/Score-4.25.2*

#Create app_prefs.dat file
RUN sudo echo -e '   <app_prefs>\n      <!-- ========================= -->\n      <!-- [program version = 4.53-RELEASE] -->\n      <!-- [timestamp = 21-Aug-2023 11:10] -->\n      <!-- [file name = /score/app_prefs.dat] -->\n      <!-- ========================= -->\n      <first_time>false</first_time>\n      <current_slug>xx</current_slug>\n      <gen_html>true</gen_html>\n      <gen_mobile_html>true</gen_mobile_html>\n      <gen_xml>true</gen_xml>\n      <gen_csv>true</gen_csv>\n      <auto_save>true</auto_save>\n      <auto_save_time_minutes>5</auto_save_time_minutes>\n      <auto_point>true</auto_point>\n      <browser_name>c:\progra~1\intern~1\iexplore.exe</browser_name>\n      <sort>true</sort>\n      <max_pilots>50</max_pilots>\n      <max_rounds>500</max_rounds>\n      <max_sequences>2</max_sequences>\n      <max_judges>8</max_judges>\n      <max_figures>12</max_figures>\n      <data_entry_cols>9</data_entry_cols>\n      <verify_imports>true</verify_imports>\n      <legacy_xml>false</legacy_xml>\n      <load_services_tab>true</load_services_tab>\n      <save_timestamp>false</save_timestamp>\n      <extended_sequences>true</extended_sequences>\n      <publish>false</publish>\n      <publish_location></publish_location>\n      <publish_protocol>file</publish_protocol>\n      <browser_launch>IMPLICIT</browser_launch>\n      <eol_type>AUTO</eol_type>\n   </app_prefs>' > /score/app_prefs.dat

#Create Score Desktop Launcher
RUN sudo mkdir /config/Desktop
RUN sudo echo -e '[Desktop Entry]\nVersion=1.0\nType=Application\nName=Score\nComment=\nExec=java -jar score.jar\nIcon=\nPath=/score\nTerminal=true\nStartupNotify=false' >> /config/Desktop/Score.desktop
RUN sudo chmod +x /config/Desktop/Score.desktop
RUN sudo chown abc:abc -R /config/Desktop
RUN sudo chown abc:abc -R /score

#ENTRYPOINT ["/init","java","-jar","/score/score.jar"]