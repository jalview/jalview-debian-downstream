<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE helpset PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN" "http://java.sun.com/products/javahelp/helpset_1_0.dtd">
<helpset version="1.0">
   <!-- title -->
   <title>Jalview Documentation</title>
   <!-- maps -->
   <maps>
     <homeID>home</homeID>
     <mapref location="help.jhm" />
   </maps>
   <!-- views -->
   <view>
      <name>TOC</name>
      <label>Table Of Contents</label>
      <type>javax.help.TOCView</type>
      <data>helpTOC.xml</data>
   </view>
  <view>
    <name>Search</name>
    <label>Search</label>
    <type>javax.help.SearchView</type>
    <data engine="com.sun.java.help.search.DefaultSearchEngine">
      JavaHelpSearch
    </data>
  </view>
<presentation default="true" displayviews="true" displayviewimages="false">
    <name>TOPALi</name>
    <size width="800" height="700" />
    <location x="200" y="50" />
    <title>Jalview Documentation</title>
    <image>helpIcon</image>
    <toolbar>
		<helpaction image="backIcon">javax.help.BackAction</helpaction>
		<helpaction image="forwardIcon">javax.help.ForwardAction</helpaction>
		<helpaction image="homeIcon">javax.help.HomeAction</helpaction>
		<helpaction>javax.help.SeparatorAction</helpaction>
		<!--<helpaction image="reloadIcon">javax.help.ReloadAction</helpaction>-->
		<!--<helpaction image="addBookmarkIcon">javax.help.FavoritesAction</helpaction>-->
		<helpaction image="printIcon">javax.help.PrintAction</helpaction>
		<helpaction image="printSetupIcon">javax.help.PrintSetupAction</helpaction>
	</toolbar>
  </presentation>
</helpset>
