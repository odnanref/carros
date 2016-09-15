/**
 * Brand and Models javascript code
 *
 */

function onBrandChange(model_id) {
    model_id = model_id || 0;
    // add models to model select
    var id = $('#make').val();
    getModelsForBrand(id, model_id);
}

function getModelsForBrand(id, model_id) {
    model_id = model_id || 0;
    //http_url = "@routes.NotificationController.getModelByBrand(31337)";
    http_url = "/getmodelbybrand/31337";
    http_url = http_url.replace("31337", id);

    $("#model").empty();

    $.get( http_url, function( data ) {
        data.forEach( function (ent) {
            $("#model").append(getOption(ent));
        });

        console.log("Olha o modelo .. " + model_id);
        if ( (model_id+0) > 0) {
            console.log("Already exists a selection @model.id " + model_id);
            $('#model').val(model_id);
        }
    });
}

function getBrands(id, model_id) {
    var id = id || 0;
    var model_id = model_id || 0;
    http_url = "/getbrands";
    $("#make").empty();

    $.get( http_url, function( data ) {
        data.forEach( function (ent) {
            $("#make").append(getOption(ent));
        });
        if (id > 0) {
            $("#make").val(id);
            if (model_id > 0 ) {
                onBrandChange(model_id);
            }
        }
    });
}

function getModelsForBrandLocal(make_id){
    $("#model").empty();
    models.forEach(function (ent) {
        if (ent.make == make_id ) {
            $("#model").append(getOption(ent));
        }
    });
}

function getOption(ent) {
    return "<option value='" + ent.id + "'>" +
        ent.descricao +
        "</option>";
}
