require(["common"], (common) ->
    require(["jquery", "dataTables", "list_util", "router", "modal_confirm"], ($, dt, lutil, router, conf) ->
        $("#main_list").dataTable(lutil.listDt(3))
        $(".hide_cutplan").click ->
            id = $(this).data("id")
            conf.confirm("confirm-modal", ->
                router.controllers.CuttingPlans.hideCuttingPlan(id).ajax
                    success: (data, status, xhr) ->
                        location.reload()
                    error: (xhr, status, err) ->
                        alert("Error hiding cutting plan: "+err)
            )
    )
)