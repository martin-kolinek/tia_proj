define(["jquery"], ($) ->
    idfn = (handle) -> "."+handle+"_id" 
    descfn = (handle) -> "."+handle+"_desc" 
    id: idfn
    desc: descfn
    init: (handle, route) ->
        $(document).ready ->
           $(descfn(handle)).each ->
                id = $(this).parent().find(idfn(handle)).val()
                if(!id)
                   return
                div = $(this)
                route(id).ajax
                    success: (data, status, xhr) ->
                        div.text(data)
                    error: (xhr, status, err) ->
                        div.text("unknown")
)