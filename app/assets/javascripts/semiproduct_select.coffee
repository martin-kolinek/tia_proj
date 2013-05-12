define(["jquery", "router", "bootstrap", "dataTables"], ($, router, bs, dt) ->
    selectSemiproduct: (modal, content, idCtl, descCtl, filter) ->
        router.controllers.Semiproducts.list(filter,"table").ajax
            error: (xhr, status, err) ->
                alert("error retrieving packs: "+err)
            success: (data, status, xhr) ->
                content.html(data)
                modal.modal("show")
                content.find("table").dataTable()
                content.find("a").click ->
                    ddl = $(this)
                    ul = ddl.parent().find("ul")
                    if(ul.children().size() == 0)
                    	router.controllers.Semiproducts.listSemiproducts(ddl.data("pack-id"), true, "dropdown").ajax
                    	    error:(xhr, status, err) ->
                    	        alert("error retrieving semiproducts: "+err)
                    	    success:(data, status, xhr) ->
                    	        ul.html(data)
                    	        ul.find("a").click ->
                    	            id = $(this).data("id")
                    	            serial = $(this).data("serial")
                    	            desc = ddl.data("desc")
                    	            idCtl.val(id)
                    	            descCtl.text(desc + " " + serial)
                    	            modal.modal("hide")
)