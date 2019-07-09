var colors = ['#5fe1fc', '#CC9966', '#FF9966', '#66FF66', '#FF3300', '#33FFFF', '#FFCCCC', '#00FFCC', '#0109CA', 'deepskyblue', '#fff']
var fontSizes = ['26']

/* -----------------第一列第一行第二行，第二列第一行当日产量，第二列第二行，第三列第一行，第三列第二行 from ZHU_LI---------------- */
function getCommandCentreBoard (res) {
  // $.ajax({
  //   type: 'POST',
  //   url: window.global.ajaxAutomatictBaseUrl + 'api/automaticintegration/board/getCommandCentreBoard',
  //   data: params,
  //   contentType: 'application/json; charset=utf-8',
  //   success: function (response) {
  //     var data = JSON.parse(response)
  //     data = data.data
  //     setRate(data)
  //     setBadistribution(data.maldistributionBoList)
  //     setLineBoardBo(data.linelist)
  //     setTotalAmout(data)
  //     setWorklineSickNum(data)
  //     setBatchBoard(data.batchBoardBoList)
  //     setPlanProduction(data)
  //   },
  //   error: function (a, b, c) {
  //     console.log(a, b, c)
  //   },
  //   complete: function (xhr) {
  //     xhr = null
  //   }
  // })
      var data=JSON.parse(res)
      setRate(data)
      // setBadistribution(data.maldistributionBoList)
      setLineBoardBo(data.linelist)
      setTotalAmout(data)
      setWorklineSickNum(data)
      setBatchBoard(data.batchBoardBoList)
      setPlanProduction(data)
}

// 设置双A率，A率，B率，C率
function setRate (data) {
  var doubleARate = echarts.init(document.getElementById('doubleARate'))
  var aRate = echarts.init(document.getElementById('aRate'))
  var bRate = echarts.init(document.getElementById('bRate'))
  var cRate = echarts.init(document.getElementById('cRate'))
  var doubleARateOption = getRateOption(data.rateAA, 'AA率(锭重)')
  var aRateOption = getRateOption(data.rateA, 'A率(锭重)')
  var bRateOption = getRateOption(data.rateB, 'B率(锭重)')
  var cRateOption = getRateOption(data.rateC, 'C率(锭重)')
  doubleARate.setOption(doubleARateOption)
  aRate.setOption(aRateOption)
  bRate.setOption(bRateOption)
  cRate.setOption(cRateOption)
}

function getRateOption (rate, title) {
  var Option = {
    title: {show: true, text: title, left: 'center', top: '0', bottom: '0', textStyle: {color: colors[10]}},
    graphic: {
      type: 'text',
      left: 'center',
      top: 'center',
      shadowColor: '#91918a',
      style: {text: rate + '%', font: 'bolder 1rem "Microsoft YaHei", sans-serif', fill: '#fff'}
    },
    series: [
      {
        name: '访问来源',
        type: 'pie',
        radius: ['43%', '55%'],
        avoidLabelOverlap: false,
        label: {normal: {show: false, position: 'center'}},
        data: [
          {value: rate, itemStyle: {normal: {color: '#5fe1fc'}}},
          {value: 100 - rate, itemStyle: {normal: {color: '#97afca'}}}
        ]
      }
    ]
  }
  return Option
}

// 设置不良品分布
function setBadistribution (maldistributionBoList) {
  if (!maldistributionBoList) {
    return
  }
  maldistributionBoList = maldistributionBoList.reverse()
  var option = {
    title: {show: true, text: '不良品分布', left: 'center', top: 'top', textStyle: {color: colors[9], fontWeight: 'normal', fontSize: fontSizes[0]}},
    series: []
  }
  var sum = 0
  var categorys = []
  for (var j = 0; j < maldistributionBoList.length; j++) {
    sum += maldistributionBoList[j].exceptionNum
    categorys.push(maldistributionBoList[j].downReasonName)
  }
  option.legend = {
    top: '12%',
    left: '20%',
    padding: 0,
    data: [],
    orient: 'vertical',
    itemHeight: 12,
    itemGap: 2,
    textStyle: { fontSize: 13, color: colors[10] }
  }
  for (var i = 0; i < maldistributionBoList.length; i++) {
    option.legend.data.push({
      name: maldistributionBoList[i].downReasonName,
      icon: 'image://../img/legend.png'
    })
    var parsent = (maldistributionBoList[i].exceptionNum / sum).toFixed(2)
    option.series.push({
      type: 'pie',
      name: maldistributionBoList[i].downReasonName,
      clockWise: true,
      radius: [i * 16, (i + 1) * 15],
      itemStyle: {normal: {label: {show: false}, labelLine: {show: false}}},
      data: [{
        value: parsent,
        itemStyle: {
          normal: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
              offset: 0,
              color: '#00B2EE'
            }, {offset: 1, color: '#00DDE6'}])
          }
        }
      }, {
        value: 1 - parsent,
        itemStyle: {
          normal: {
            color: 'transparent'
          }
        }
      }]
    })
  }
  option.legend.data = option.legend.data.reverse()
  var defectiveRate = echarts.init(document.getElementById('defectiveRate'))
  defectiveRate.setOption(option)
}

// 设置单线降等
function setLineBoardBo (list) {
  var catetoryData = []
  var seriesData = [
    {
      name: '总数',
      data: [],
      type: 'bar',
      itemStyle: {
        normal: {
          barBorderRadius: 3,
          borderRadius: 200,
          color: new echarts.graphic.LinearGradient(
            0, 0, 0, 1,
            [
              {offset: 0, color: '#cbf0f8'},
              {offset: 1, color: '#042dad'}

            ]
          )
        }
      },
      label: {normal: {show: true, position: 'top', color: '#fff'}}
    },
    {
      name: '异常数',
      data: [],
      type: 'bar',
      itemStyle: {
        barBorderRadius: 3,
        borderRadius: 200,
        normal: {
          color: new echarts.graphic.LinearGradient(
            0, 0, 0, 1,
            [
              {offset: 0, color: '#d0e7f8'},
              {offset: 1, color: '#e6080f'}

            ]
          )
        }
      },
      label: {normal: {show: true, position: 'top', color: '#fff'}}
    }]
  if (list) {
    for (var i = 0; i < list.length; i++) {
      catetoryData.push(list[i].line + '线')
      seriesData[0].data.push(list[i].amout)
      seriesData[1].data.push(list[i].exceptionNum)
    }
  }
  var singleDefectiveRateOption = {
    title: {show: true, text: '单线降等(锭)', left: 'left', top: 'top', textStyle: {color: colors[9], fontSize: fontSizes[0], fontWeight: 'normal'}},
    grid: {show: true, borderColor: '#33FFFF', left: 0, right: 0},
    xAxis: [{
      type: 'category',
      data: catetoryData,
      axisLabel: {show: true, textStyle: {color: '#fff', fontSize: '12'}}
    }],
    yAxis: [{type: 'value', axisLabel: {show: true, textStyle: {color: '#fff', fontSize: '12'}}}],
    series: seriesData
  }
  // var singleDefectiveRate = echarts.init(document.getElementById('singleDefectiveRate'))
  // singleDefectiveRate.setOption(singleDefectiveRateOption)
}

// 当日产量，待包装数，打包锭数，打包包数
function setTotalAmout (data) {
  if (data) {
    var totalAmout = data.totalAmout.toString().padStart(6, '0')
    var resultStr = ''
    for (var i = 0; i < 6; i++) {
      resultStr += '<li>' + totalAmout[i] + '</li>'
    }
    $('#totalAmout').html(resultStr)
    $('#waitPackNum').text(data.waitPackNum)
    $('#packedNum').text(data.packedNum)
    $('#boxNum').text(data.boxNum)
  }
}
// 自动1包装锭数，自动2包装锭数，自动3包装锭数
function setWorklineSickNum (data) {
  var autoline = echarts.init(document.getElementById('autoline'))
  var autolineOption = {
    series: [
      {
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        label: {normal: {show: true, color: colors[10], formatter: '{b}\n{c}锭', fontSize: 15}},
        labelLine: {normal: {show: false}},
        data: [
          {value: data.autoPackNum1, name: '自动1线'},
          {value: data.autoPackNum2, name: '自动2线'},
          {value: data.autoPackNum3, name: '自动3线'},
          {value: data.handpackNum, name: '人工线'}
        ]
      }
    ]
  }
  autoline.setOption(autolineOption)
}

/* ---------------------第一列第三行异常 from ZHU_LI------------------------ */
var page = {current: 1, size: 6, total: 0}

function getSilkAlarmList (list) {
  // response = JSON.parse(response)
      $('#currTime').text((new Date().toISOString()).substr(0, 10))
      var tbodyHtml = ''
      for (var i = 0; i < list.length; i++) {
        tbodyHtml += '<tr>'
        tbodyHtml += '<td>' + (i < list.length ? list[i].lineName : '&nbsp;') + '</td>'
        tbodyHtml += '<td>' + (i < list.length ? list[i].AA : '&nbsp;') + '</td>'
        tbodyHtml += '<td>' + (i < list.length ? list[i].A : '&nbsp;') + '</td>'
        tbodyHtml += '<td>' + (i < list.length ? list[i].B : '&nbsp;') + '</td>'
        tbodyHtml += '<td>' + (i < list.length ? list[i].C : '&nbsp;') + '</td>'
        // tbodyHtml += '<td>' + (i < list.length ? (list[i].status === '1' ? '未处理' : '已处理') : '&nbsp;') + '</td>'
        tbodyHtml += '</tr>'
      }
      $('#sickAlarmTable').html(tbodyHtml)
      // page.current = ((page.current + 1) > Math.ceil(response.data.count / page.size)) ? 1 : page.current + 1
  // var params = {
  //   status: '1',
  //   workshopId: workShopID,
  //   pageIndex: page.current,
  //   pageCount: page.size
  // }
  // params = JSON.stringify(params)
  // $.ajax({
  //   type: 'POST',
  //   url: window.global.ajaxAutomatictBaseUrl + 'api/automaticintegration/board/getCommandSilkAlarmList',
  //   data: params,
  //   contentType: 'application/json; charset=utf-8',
  //   success: function (response) {
  //     response = JSON.parse(response)
  //     $('#currTime').text((new Date(response.data.systemDate).toISOString()).substr(0, 10))
  //     var list = response.data.list
  //     var tbodyHtml = ''
  //     for (var i = 0; i < page.size; i++) {
  //       tbodyHtml += '<tr>'
  //       tbodyHtml += '<td>' + (i < list.length ? list[i].batchNo : '&nbsp;') + '</td>'
  //       tbodyHtml += '<td>' + (i < list.length ? list[i].lineName : '&nbsp;') + '</td>'
  //       tbodyHtml += '<td>' + (i < list.length ? list[i].item : '&nbsp;') + '</td>'
  //       tbodyHtml += '<td>' + (i < list.length ? list[i].fallNo : '&nbsp;') + '</td>'
  //       tbodyHtml += '<td>' + (i < list.length ? list[i].spidleNo : '&nbsp;') + '</td>'
  //       tbodyHtml += '<td>' + (i < list.length ? list[i].downGradeReasonName : '&nbsp;') + '</td>'
  //       // tbodyHtml += '<td>' + (i < list.length ? (list[i].status === '1' ? '未处理' : '已处理') : '&nbsp;') + '</td>'
  //       tbodyHtml += '</tr>'
  //     }
  //     $('#sickAlarmTable').html(tbodyHtml)
  //     page.current = ((page.current + 1) > Math.ceil(response.data.count / page.size)) ? 1 : page.current + 1
  //   },
  //   error: function (a, b, c) {
  //     console.log(a, b, c)
  //   },
  //   complete: function (xhr) {
  //     xhr = null
  //   }
  // })
}

/* ------------------第二列第一行入库，出库 form LIN_KE------------------- */
function getDailyProductionByWorkshopId (res) {
      $('#retrievalNum').text(res.retrievalNum)
      $('#stockingNum').text(res.stockingNum)
  // $.ajax({
  //   type: 'POST',
  //   url: window.global.ajaxWarehouseManagementBaseUrl + 'api/warehouseManagement/board/getDailyProductionByWorkshopId',
  //   data: params,
  //   contentType: 'application/json; charset=utf-8',
  //   success: function (response) {
  //     var data = JSON.parse(response)
  //     data = data.data
  //     $('#retrievalNum').text(data.retrievalNum)
  //     $('#stockingNum').text(data.stockingNum)
  //   },
  //   error: function (a, b, c) {
  //     console.log(a, b, c)
  //   },
  //   complete: function (xhr) {
  //     xhr = null
  //   }
  // })
}

/* ------------------第二列第三行已完成订单 form LIN_KE------------------ */
function getOrderInfo (params) {
  $.ajax({
    type: 'POST',
    url: window.global.ajaxWarehouseManagementBaseUrl + 'api/warehouseManagement/board/getOrderInfo',
    data: params,
    contentType: 'application/json; charset=utf-8',
    success: function (response) {
      var data = JSON.parse(response)
      data = data.data
      // data.finishedCount = 3
      // data.allCount = 7
      $('#finishedCount').text(data.finishedWeight)
      $('#allCount').text(data.allWeight)
      $('#outter>#inner').css('height', ((data.finishedCount / data.allCount).toFixed(2) * 100).toString() + '%')

      var htmlStr = ''
      for (var i = 0; i < 8; i++) {
        htmlStr += (i < data.itemList.length)
          ? '<tr><td>TOP' + (i + 1) + '</td>' +
          '<td>' + data.itemList[i].batchNo + '</td>' +
          '<td>' + data.itemList[i].weight + '(kg)</td></tr>'
          : '<tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>'
      }
      $('#maldistributionBoList').html(htmlStr)
    },
    error: function (a, b, c) {
      console.log(a, b, c)
    },
    complete: function (xhr) {
      xhr = null
    }
  })
}

/* -----------------------------第三列第一行 from ZHU_LI------------------------------------ */
var flag = true

function setBatchBoard (data) {
  if (data) {
    // flag = !flag
    var htmlStr = ''
    for (var i = (flag ? 0 : 10); i < (flag ? 10 : 20); i++) {
      htmlStr += (i < data.length)
        ? '<tr><td>' + data[i].batchNo + '</td>' +
        getBatchLine(data[i].list) +
        '<td>' + data[i].amout + '</td></tr>'
        : '<tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>'
    }
    function getBatchLine (list) {
      return list.length > 4
        ? '<td style="word-break: keep-all;white-space:nowrap;font-size: .8rem;">' + list.join(',') + '</td>'
        : '<td>' + list.join(',') + '</td>'
    }

    $('#batchBoardBoList').html(htmlStr)
  }
}

/* ----------------------------------------------第三列第二行------------------------------- */
function setPlanProduction (data) {
  $('#planAmout').text(data.planAmout)
  $('#fallNum').text(data.fallNum + '锭')
  $('#stripNum').text(data.stripNum + '锭')
  $('#measureNum').text(data.measureNum + '锭')
  $('#hosieryNum').text(data.hosieryNum + '锭')
  $('#sentenceNum').text(data.sentenceNum + '锭')
  $('#surfaceNum').text(data.surfaceNum + '锭')
  $('#handpackNum').text(data.handpackNum + '锭')
  $('#autoPackNum').text(data.autoPackNum + '锭')
}

/* ------------------第三列第三行库位实时状况 form LIN_KE------------------- */
function getStorageViewStatus (params) {
  $.ajax({
    type: 'POST',
    url: window.global.ajaxWarehouseManagementBaseUrl + 'api/warehouseManagement/storage/getStorageViewStatus',
    data: params,
    contentType: 'application/json; charset=utf-8',
    success: function (response) {
      var data = JSON.parse(response)
      data = data.data

      var dataArr = Array(96)
      var index = 0
      var strHtml = ''
      for (var i = 0; i < data.length; i++) {
        index = data[i].areaX - 1 + (data[i].areaY - 1) * 16
        dataArr[index] = data[i]
      }
      for (var i = 0; i < dataArr.length; i++) {
        strHtml += '<li class="' + dataArr[i].storageStockStatus.toLowerCase() + '">' + (dataArr[i].storageStockStatus === 'EMPTY' ? '&nbsp;' : dataArr[i].num) + '</li>'
      }
      $('#storageViewStatus').html(strHtml)
    },
    error: function (a, b, c) {
      console.log(a, b, c)
    },
    complete: function (xhr) {
      xhr = null
    }
  })
}
