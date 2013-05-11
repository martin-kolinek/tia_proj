define(["jquery"], ($) ->
    idfn = (handle) -> "."+handle+"_id" 
    descfn = (handle) -> "."+handle+"_desc" 
    id: idfn
    desc: descfn
    init: (handle, route) ->
        $(document).ready ->
           $(descfn(handle)).each ->
                idinp = $(this).parent().find(idfn(handle)) 
                id = idinp.val()
                if(!id)
                   return
                div = $(this)
                route(id).ajax
                    success: (data, status, xhr) ->
                        div.text(data.desc)
                        for key, val of data
                            idinp.data(key, val)
                    error: (xhr, status, err) ->
                        div.text("unknown")
)