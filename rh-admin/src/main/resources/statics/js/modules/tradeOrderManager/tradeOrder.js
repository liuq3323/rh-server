$(function () {
    $("#jqGrid").jqGrid({
        url: '../../tradeOrder/list',
        sortname: 'createtime',
        sortable:true,
        sortorder: 'desc',
        datatype: "json",
        type: 'post',
        colModel: [
            { label: '商户ID', name: 'merchantId', index: 'merchantId', width: 20 },
            { label: '序号', name: 'id', index: 'id', width: 20 },
            { label: '订单号', name: 'orderId', index: 'orderId', width: 50 },
            { label: '订单金额', name: 'orderAmount', index: 'orderAmount', width: 70 },
            { label: '订单状态', name: 'status', index: 'status', width: 30 },
            { label: '通知状态', name: 'notifyStatus', index: 'notifyStatus', width: 30 },
            { label: '支付方式', name: 'payType', index: 'payType', width: 80 },
            // { label: '创建时间', name: 'createTime', index: 'createTime', width: 80 },
            { label: '订单生成日期',
                name: 'createTime',
                // sortorder: 'desc',
                index: 'createTime',
                width: 80,
                formatter: function (cellvalue, options, row) {
                    return new Date(cellvalue).toLocaleString()
                }}
        ],
        viewrecords: true,
        height: 600,
        rowNum: 25,
        rowList : [30,50,80],
        rownumbers: true,
        rownumWidth: 25,
        autowidth:true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames : {
            page:"page",
            rows:"limit",
            order: "order"
        },
        gridComplete:function(){
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" });
        },
    });
    //配置状态字段背景色
    // function addCellAttr(rowId, val, rawObject, cm, rdata) {
    //     if (rawObject.state == '可选') {
    //         return "style='background-color:#00FF00'";
    //     }
    //     if (rawObject.state == '已选') {
    //         return "style='background-color:#F0E68C'";
    //     }
    //     if (rawObject.state == '废号') {
    //         return "style='background-color:#990033'";
    //     }
    // }
});

var vm = new Vue({
    el:'#rrapp',
    data:{
        q:{
            tradeid:null,
            orderid: null,
            merchantid:null,
            starttime:null,
            endtime:null,
            tradestatus:null
        },
        showList: true,
        title: null,
        abd: {}
    },
    methods: {
        query: function () {
            vm.reload2();
        },
        use: function(){
            var id = getSelectedRow();
            if(id == null){
                return ;
            }
            var rowData = $("#jqGrid").jqGrid('getRowData',id);
            if ("废号"==rowData.state){
                alert("废号不可选");
                return ;
            }
            $.ajax({
                type: "POST",
                url: baseURL + "abd/use",
                contentType: "application/json",
                data: JSON.stringify(id),
                success: function(r){
                    if(r.code === 0){
                        alert('操作成功', function(index){
                            vm.reload2();
                        });
                    }else{
                        alert(r.msg);
                    }
                }
            });
        },
        refund: function (event) {
            var id = getSelectedRow();
            if(id == null){
                return ;
            }
            var rowData = $("#jqGrid").jqGrid('getRowData',id);
            if ("废号"==rowData.state){
                alert("废号不可退");
                return ;
            }
            $.ajax({
                type: "POST",
                url: baseURL + "abd/refund",
                contentType: "application/json",
                data: JSON.stringify(id),
                success: function(r){
                    if(r.code === 0){
                        alert('操作成功', function(index){
                            vm.reload2();
                        });
                    }else{
                        alert(r.msg);
                    }
                }
            });
        },
        useless: function() {
            var id = getSelectedRow();
            if(id == null){
                return ;
            }
            $.ajax({
                type: "POST",
                url: baseURL + "abd/useless",
                contentType: "application/json",
                data: JSON.stringify(id),
                success: function(r){
                    if(r.code === 0){
                        alert('操作成功', function(index){
                            vm.reload2();
                        });
                    }else{
                        alert(r.msg);
                    }
                }
            });
        },
        saveOrUpdate: function (event) {
            var url = vm.abd.id == null ? "abd/save" : "abd/update";
            $.ajax({
                type: "POST",
                url: baseURL + url,
                contentType: "application/json",
                data: JSON.stringify(vm.abd),
                success: function(r){
                    if(r.code === 0){
                        alert('操作成功', function(index){
                            vm.reload();
                        });
                    }else{
                        alert(r.msg);
                    }
                }
            });
        },
        del: function (event) {
            var ids = getSelectedRows();
            if(ids == null){
                return ;
            }

            confirm('确定要删除选中的记录？', function(){
                $.ajax({
                    type: "POST",
                    url: baseURL + "abd/delete",
                    contentType: "application/json",
                    data: JSON.stringify(ids),
                    success: function(r){
                        if(r.code == 0){
                            alert('操作成功', function(index){
                                $("#jqGrid").trigger("reloadGrid");
                            });
                        }else{
                            alert(r.msg);
                        }
                    }
                });
            });
        },
        getInfo: function(id){
            $.get(baseURL + "abd/info/"+id, function(r){
                vm.abd = r.abd;
            });
        },
        reload: function (event) {
            vm.showList = true;
            $("#jqGrid").jqGrid('clearGridData');
            $("#jqGrid").jqGrid('setGridParam',{
                postData:{'tradeid': $("#tradeid").val(),
                    'orderid':$("#orderid").val(),
                    'starttime' : $("#starttime").val(),
                    'endtime' : $("#endtime").val(),
                    'merchantid' : $("#merchantid").val(),
                    'status' : $("#status").val()},
                page:1
            }).trigger("reloadGrid");
        },
        reload2: function (event) {
            vm.showList = true;
            var page = $("#jqGrid").jqGrid('getGridParam','page');
            $("#jqGrid").jqGrid('setGridParam',{
                postData:{'tradeid': $("#tradeid").val(),
                    'orderid':$("#orderid").val(),
                'starttime' : $("#starttime").val(),
                'endtime' : $("#endtime").val(),
                'merchantid' : $("#merchantid").val(),
                'status' : $("#status").val()
                },
                page:page
            }).trigger("reloadGrid");
        }
    }
});