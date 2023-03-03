package org.telegram.telegrise.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.Element;
import org.telegram.telegrise.core.parser.ElementField;
import org.telegram.telegrise.core.parser.InnerElement;

import java.util.List;

@Element(name = "menu")
@Data
@NoArgsConstructor
public class Menu implements BranchingElement{
    @ElementField(name = "name", nullable = false)
    private String name;
    @ElementField(name = "type")
    private String type;

    @InnerElement(nullable = false)
    private List<Tree> trees;

    public Tree findTree(ResourcePool pool){
        return trees.stream().filter(t -> t.canHandle(pool)).findFirst().orElse(null);
    }
}
