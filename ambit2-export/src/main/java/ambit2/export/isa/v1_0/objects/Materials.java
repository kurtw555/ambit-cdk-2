package ambit2.export.isa.v1_0.objects;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

//class created for property: materials
//schema: assay_schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("ambit.json2pojo")
@JsonPropertyOrder({
    "samples",
    "otherMaterials"
})
public class Materials
{
    @JsonProperty("samples")
    public List<Sample> samples = new ArrayList<Sample>();
    @JsonProperty("otherMaterials")
    public List<Material> otherMaterials = new ArrayList<Material>();
}
