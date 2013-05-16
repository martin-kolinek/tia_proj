requirejs.config
    paths:
        "dataTables" : "jquery.dataTables"
        "router" : "jsRoutes"
        "datepicker" : "bootstrap-datepicker"
    waitSeconds : 20
    shim:
        "dataTables" : ["jquery"]
        "bootstrap" : ["jquery"]
        "underscore" :
            exports : "_"
        "router" : 
            deps : ["jquery"]
            exports : "jsRouter"
        "datepicker" : ["jquery", "bootstrap"]
