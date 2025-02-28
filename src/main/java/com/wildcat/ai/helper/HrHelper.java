package com.wildcat.ai.helper;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class HrHelper {
    @Tool("Esto Permite Obtener La Edad En Base Al Nombre.")
    public String getAge(@P(value = "Este Es El Nombre Del Cual Se Quiere Obtener La Edad") String name){
        return "";
    }

}