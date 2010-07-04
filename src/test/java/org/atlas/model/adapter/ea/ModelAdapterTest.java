package org.atlas.model.adapter.ea;

import java.io.File;
import org.atlas.mda.Context;
import org.atlas.mda.ModelInput;
import org.atlas.mda.ModelTransformer;
import org.atlas.mda.TransformException;
import org.atlas.mda.adapter.AdapterException;
import org.atlas.metamodel.Model;
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