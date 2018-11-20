$(function () {
    $("#jqGrid").jqGrid({
        url: baseURL + 'merchant/mgr/list',
        datatype: "json",
        colModel: [
            {label: '商户ID', name: 'id', index: "id", width: 45, key: true},
            {label: '商户名称', name: 'merchantName', sortable: false, width: 75},
            {label: '上级商户', name: 'merchantDeptName', sortable: false, width: 75},
            {label: '联系方式', name: 'merchantPhone', sortable: false, width: 75},
            {label: '商户密钥', name: 'merchantKey', width: 90},
            // { label: '手机号', name: 'mobile', width: 100 },
            {
                label: '状态', name: 'status', width: 60, formatter: function (value, options, row) {
                    return value === 0 ?
                        '<span class="label label-success">通过验证</span>' :
                        '<span class="label label-danger">其他</span>';
                }
            },
            // { label: '创建时间', name: 'createTime', index: "create_time", width: 85},
            {
                label: '商户交易权限', name: 'tradeStatus', width: 65,
                formatter: function (value, options, row) {
                    return value === 0 ?
                        '<span class="label label-success pointer" onclick="vm.updateTradeStatus(\'' + value + '\',\'' + row.id + '\')">关闭交易</span>' :
                        '<span class="label label-danger pointer" onclick="vm.updateTradeStatus(\'' + value + '\',\'' + row.id + '\')">开启交易</span>';
                }
            },
            {
                label: '通道配置', name: 'id', width: 65,
                formatter: function (value, options, row) {
                    return '<span class="label label-success pointer" onclick="vm.audit(\'' + value + '\')">详情</span>'
                }
            }
        ],
        viewrecords: true,
        height: 720,
        rowNum: 10,
        rowList: [10, 30, 50],
        rownumbers: true,
        rownumWidth: 25,
        autowidth: true,
        multiselect: false,
        pager: "#jqGridPager",
        jsonReader: {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames: {
            page: "page",
            rows: "limit",
            order: "order"
        },
        gridComplete: function () {
            // var ids=$("#jqGrid").jqGrid('getDataIDs');
            //隐藏grid底部滚动条
            $("#jqGrid").closest(".ui-jqgrid-bdiv").css({"overflow-x": "hidden"});
        }
    });
    vm.loadAppIdSelect();
});
var setting = {
    data: {
        simpleData: {
            enable: true,
            idKey: "deptId",
            pIdKey: "parentId",
            rootPId: -1
        },
        key: {
            url: "nourl"
        }
    }
};
var ztree;

var vm = new Vue({
    el: '#rrapp',
    data: {
        q: {
            username: null
        },
        showList: true,
        showList2: false,
        showList3: false,
        title: null,
        roleList: {},
        user: {
            status: 1,
            deptId: null,
            deptName: null,
            roleIdList: []
        },
        merchant: {
            id: null,
            merchantName: null,
            merchantDeptId: null,
            merchantPhone: null,
            pid: null,
            storeNumber: null,
            authCode: null,
            appid: null,
            aliPubKey: null,
            merchantPubKey: null,
            merchantPriKey: null,
            merchantDeptName: null,
            mobileUrl: null,
            wechantNum: null,
            wechantKey: null
        }
    },
    methods: {
        query: function () {
            vm.reload();
        },
        loadAppIdSelect: function () {
            $("select[name='appidSelect']").empty();
            $.ajax({
                type: "POST",
                url: baseURL + "merchant/appid/queryAppid",
                contentType: "application/json",
                success: function (r) {
                    if (r.code == 0) {
                        console.log(r.appId);
                        for (var i = 0; i < r.appId.length; i++) {
                            $("select[name='appidSelect']").append("<option value='" + r.appId[i] + "'>" + r.appId[i] + "</option>");
                        }
                    } else {
                        alert(r.msg);
                    }
                }
            });
        },
        changeType: function (ele) {
            console.log("---->select onchange function");
            var optionTxt = $(ele.target).find("option:selected").text();
            var optionVal = ele.target.value;
            var param = {"appid": optionVal};
            $.ajax({
                type: "POST",
                url: baseURL + "merchant/appid/queryInfoByAppid",
                contentType: "application/json",
                data: JSON.stringify(param),
                success: function (r) {
                    if (r.code == 0) {
                        console.log(r.appId);
                        vm.merchant.aliPubKey = r.appId.aliPubKey;
                        vm.merchant.merchantPubKey = r.appId.merchantPubKey;
                        vm.merchant.merchantPriKey = r.appId.merchantPriKey;
                        vm.merchant.appid = optionVal;
                    } else {
                        alert(r.msg);
                    }
                }
            });
        },
        audit: function (value) {
            var id = value;
            if (id == null) {
                return;
            }

            vm.showList = false;
            vm.showList2 = true;
            vm.title = "详情";

            vm.getInfo(id);
            //获取角色信息
            this.getRoleList();
            //下拉列表选中改选项的appid
            $("select[name='appidSelect']").find("option[value = '" + yue + "']").attr("selected", "selected");
        },

        add: function () {
            vm.showList = false;
            vm.showList3 = true;
            vm.title = "新增";
            vm.roleList = {};
            vm.merchant = {
                merchantName: null,
                merchantDeptId: null,
                merchantPhone: null,
                pid: null,
                storeNumber: null,
                authCode: null,
                appid: null,
                aliPubKey: null,
                merchantPubKey: null,
                merchantPriKey: null,
                merchantDeptName: null,
                mobileUrl: null,
                wechatNum: null,
                wechatKey: null
            };
            vm.user = {deptName: null, deptId: null, status: 1, roleIdList: []};

            //获取角色信息
            this.getRoleList();

            vm.getDept();
        },
        getDept: function () {
            //加载部门树
            $.get(baseURL + "merchant/dept/selectParent", function (r) {
                ztree = $.fn.zTree.init($("#deptTree"), setting, r.deptList);
                var node = ztree.getNodeByParam("deptId", vm.user.deptId);
                if (node != null) {
                    ztree.selectNode(node);
                    vm.merchant.merchantDeptId = node.name;
                    // vm.merchant.merchantdeptid = node.id;
                }
            })
        },
        update: function () {
            var userId = getSelectedRow();
            if (userId == null) {
                return;
            }

            vm.showList = false;
            vm.showList3 = true;
            vm.title = "修改";

            vm.getUser(userId);
            //获取角色信息
            this.getRoleList();
        },
        del: function () {
            var userIds = getSelectedRows();
            if (userIds == null) {
                return;
            }

            confirm('确定要删除选中的记录？', function () {
                $.ajax({
                    type: "POST",
                    url: baseURL + "sys/user/delete",
                    contentType: "application/json",
                    data: JSON.stringify(userIds),
                    success: function (r) {
                        if (r.code == 0) {
                            alert('操作成功', function () {
                                vm.reload();
                            });
                        } else {
                            alert(r.msg);
                        }
                    }
                });
            });
        },
        saveOrUpdate: function () {
            var url = vm.merchant.id == null ? "merchant/mgr/save" : "merchant/mgr/update";
            $.ajax({
                type: "POST",
                url: baseURL + url,
                contentType: "application/json",
                data: JSON.stringify(vm.merchant),
                success: function (r) {
                    if (r.code === 0) {
                        alert('操作成功', function () {
                            vm.reload();
                        });
                    } else {
                        alert(r.msg);
                    }
                }
            });
        },
        getUser: function (userId) {
            $.get(baseURL + "sys/user/info/" + userId, function (r) {
                vm.user = r.user;
                vm.user.password = null;

                vm.getDept();
            });
        },
        getInfo: function (id) {
            $.get(baseURL + "merchant/mgr/info/" + id, function (r) {
                vm.merchant = r.merchant;
                //下拉列表选中改选项的appid
                $("select[name='appidSelect']").find("option[value = '" + r.merchant.appid + "']").attr("selected", "selected");
                vm.getDept();
            });
        },
        updateTradeStatus: function (value, id) {
            // console.log(value);//开启状态传来的value为0
            // console.log(id);
            confirm('确定操作商户交易权限？', function () {
                var dataMap = '{"merchantId":"' + id + '" , "tradeStatus":"' + value + '"}';
                $.ajax({
                    type: "POST",
                    url: baseURL + "merchant/mgr/tradestatus",
                    contentType: "application/json",
                    data: dataMap,
                    success: function (r) {
                        if (r.code === 0) {
                            alert('操作成功', function () {
                                vm.reload();
                            });
                        } else {
                            alert(r.msg);
                        }
                    }
                });
            });
        },
        getRoleList: function () {
            $.get(baseURL + "sys/role/select", function (r) {
                vm.roleList = r.list;
            });
        },
        deptTree: function () {
            layer.open({
                type: 1,
                offset: '50px',
                skin: 'layui-layer-molv',
                title: "选择部门",
                area: ['300px', '450px'],
                shade: 0,
                shadeClose: false,
                content: jQuery("#deptLayer"),
                btn: ['确定', '取消'],
                btn1: function (index) {
                    var node = ztree.getSelectedNodes();
                    //选择上级部门
                    vm.merchant.merchantDeptName = node[0].name;
                    vm.merchant.merchantDeptId = node[0].deptId;

                    layer.close(index);
                }
            });
        },
        reload: function () {
            vm.showList = true;
            vm.showList2 = false;
            vm.showList3 = false;
            var page = $("#jqGrid").jqGrid('getGridParam', 'page');
            $("#jqGrid").jqGrid('setGridParam', {
                postData: {'id': vm.merchant.id},
                page: page
            }).trigger("reloadGrid");
            vm.loadAppIdSelect();
        }
    }
});