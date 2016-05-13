<?xml version="1.0" encoding="UTF-8" ?>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

  <!-- Set context path -->
  <c:set var="context" value="${pageContext.request.contextPath}" />


  <!-- LOAD BOOTSTRAP -->
  <link rel="stylesheet" href="${context}/bootstrap/css/bootstrap.min.css"/>
  <link rel="stylesheet" href="${context}/bootstrap/css/bootstrap-theme.css"/>
  <link rel="stylesheet" href="${context}/bootstrap/css/bootstrap-datepicker.min.css"/>
  <link rel="stylesheet" href="${context}/stylesheets/jquery-ui.min.css"/>
  <link rel="stylesheet" href="${context}/stylesheets/style.css"/>
  <link rel="stylesheet" href="${context}/css/buttonOverrides.css"/>

  <%--This needs to be here..--%>
  <script src="${context}/js/jquery.2.1.3.min.js"></script>
  <script src="${context}/js/jquery-ui.min.js"></script>
  <script src="${context}/js/scripts.js"></script>

  <title>Aviation Reporting Attendance Summary Page</title>
</head>
<body onload="val = $('#sectionId option:first').val() ; toggleSection(val);">
<form class="sectionDropdown" method="post" action="DoNotActuallyPost">
  <label>
    <form:select class="form-control" id="sectionId" path="sectionId" items="${sectionList}" itemValue="id"  itemLabel="name" onchange="toggleSection(value); false;"/>
  </label>
</form>
<a id="showRoster" href="${context}/showRoster/">Back to Roster</a>
  <br/>

  <div class="container">

    <c:forEach items="${attendanceSummaryForSections}" var="summaryForSection" varStatus="loop">
        <table class="table table-bordered sectionTable" style="display:none" id="${summaryForSection.sectionId}">
          <tr>
            <th>Name</th>
            <th>Minutes Made Up</th>
            <th>Remaining Minutes Made up</th>
            <th>Total Minutes Missed</th>
            <th>% of Course Missed</th>
          </tr>

          <c:forEach items="${summaryForSection.entries}" var="attendancesummaryEntry" varStatus="loop">
            <tr >
              <td><a href="${context}/studentMakeup/${attendancesummaryEntry.sectionId}/${attendancesummaryEntry.studentId}">${attendancesummaryEntry.studentName}</a></td>
              <td>${attendancesummaryEntry.sumMinutesMadeup}</td>
              <td>${attendancesummaryEntry.remainingMinutesMadeup}</td>
              <td>${attendancesummaryEntry.sumMinutesMissed}</td>
              <td>${attendancesummaryEntry.percentCourseMissed}</td>
            </tr>
          </c:forEach>
        </table>
    </c:forEach>

  </div>

</body>
</html>