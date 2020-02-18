<h2>Crear Indicador</h2>
   <p id="status"></p>
  <form action="" method="POST" role="form">
 
    <div class="form-group">
      <label for="nombre">Nombre </label>
      <input type="text" class="form-control" id="nombre" name="nombre" placeholder="debe comenzar con i_">
    </div>
    <div class="form-group">
      <label for="expresion">Expresion</label>
      <input type="text" class="form-control" id="expresion" name="expresion" placeholder="Ingrese la expresion del indicador">
    
     <div class="form-group">
        <input type="submit" id="register" value="Crear indicador" disabled="disabled" />
          </div>
  </form>

<script>
$('#nombre,#expresion').bind('keyup', function() {
    if(allFilled()) $('#register').removeAttr('disabled');
});
</script>
<script>
function allFilled() {
    var filled = true;
    $('body input').each(function() {
        if($(this).val() == '') filled = false;
    });
    return filled;
}
</script>
<!-- Simple JS Function to convert the data into JSON and Pass it as ajax Call --!>
<script>
$(function() {
    $('form').submit(function(e) {
        e.preventDefault();
        var this_ = $(this);
        var array = this_.serializeArray();
        var json = {};
    
        $.each(array, function() {
            json[this.name] = this.value || '';
        });
        json = JSON.stringify(json);
    
        // Ajax Call
        $.ajax({
            type: "POST",
            url: "crearIndicador",
            data: json,
            dataType: "json",
            success : function() {
                $("#status").text("Indicador creado exitosamente");
                this_.find('input,select').val('');
            },
            error : function(e) {
                console.log(e.responseText);
                $("#status").text(e.responseText);
            }
        });
        $("html, body").animate({ scrollTop: 0 }, "slow");
        return false;
    });
});

</script>