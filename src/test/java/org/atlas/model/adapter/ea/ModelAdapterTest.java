package org.atlas.model.adapter.ea;

import java.io.File;
import org.atlas.engine.Context;
import org.atlas.engine.ModelInput;
import org.atlas.engine.ModelTransformer;
import org.atlas.engine.TransformException;
import org.atlas.model.adapter.AdapterException;
import org.atlas.model.metamodel.Model;
import org.junit.Test;

/**
 *
 * @author andrews
 */
public class ModelAdapterTest {

    public ModelAdapterTest() {
    }

    /**
     * Test of adapt method, of class ModelAdapter.
     */
    @Test
    public void testAdapt() throws TransformException, AdapterException {
        System.out.println("adapt");
        ModelInput m = Context.getModelInputs().get(0);
        File file = new File(m.getFile());
        ModelAdapter adapter = new ModelAdapter();
        Model model = adapter.adapt(file, new Model());

    }

    @Test
    public void testTransform() throws TransformException {
        ModelTransformer t = new ModelTransformer();
        t.transform();
    }

}