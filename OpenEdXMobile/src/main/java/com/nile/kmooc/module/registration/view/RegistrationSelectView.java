package com.nile.kmooc.module.registration.view;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.nile.kmooc.logger.Logger;
import com.nile.kmooc.module.registration.model.RegistrationFormField;
import com.nile.kmooc.module.registration.model.RegistrationOption;

class RegistrationSelectView implements IRegistrationFieldView {

    protected static final Logger logger = new Logger(RegistrationEditTextView.class);
    private RegistrationFormField mField;
    private View mView;
    private RegistrationOptionSpinner mInputView;
    private TextView mInstructionsView;
    private TextView mErrorView;

    public RegistrationSelectView(RegistrationFormField field, View view) {
        // create and configure view and save it to an instance variable
        this.mField = field;
        this.mView = view;

        this.mInputView = (RegistrationOptionSpinner) view.findViewById(com.nile.kmooc.R.id.input_spinner);
        this.mInstructionsView = (TextView) view.findViewById(com.nile.kmooc.R.id.input_spinner_instructions);
        this.mErrorView = (TextView) view.findViewById(com.nile.kmooc.R.id.input_spinner_error);

        // set prompt
        mInputView.setPrompt(mField.getLabel());

        RegistrationOption defaultOption = null;
        for (RegistrationOption option : mField.getOptions()) {
            if (option.isDefaultValue()) {
                defaultOption = option;
                break;
            }
        }
        mInputView.setItems(mField.getOptions(),defaultOption);

        setInstructions(field.getInstructions());

        // hide error text view
        mErrorView.setVisibility(View.GONE);

        // This tag is necessary for End-to-End tests to work properly
        mInputView.setTag(mField.getName());
    }

    @Override
    public JsonElement getCurrentValue() {
        // turn text view content into a JsonElement and return it
        return new JsonPrimitive(mInputView.getSelectedItemValue());
    }

    public boolean setRawValue(String value){
        if ( mInputView.hasValue( value ) ){
            mInputView.select( value );
            return true;
        }
        return false;
    }

    @Override
    public boolean hasValue() {
        return (mInputView.getSelectedItem() != null
                && !TextUtils.isEmpty(mInputView.getSelectedItemValue()));
    }

    @Override
    public RegistrationFormField getField() {
        return mField;
    }

    @Override
    public View getView() {
        return mView;
    }

    @Override
    public void setInstructions(@Nullable String instructions) {
        if (instructions != null && !instructions.isEmpty()) {
            mInstructionsView.setVisibility(View.VISIBLE);
            mInstructionsView.setText(instructions);
        }
        else {
            mInstructionsView.setVisibility(View.GONE);
        }
    }

    @Override
    public void handleError(String error) {
        if (error != null && !error.isEmpty()) {
            mErrorView.setVisibility(View.VISIBLE);
            mErrorView.setText(error);
        }
        else {
            logger.warn("error message not provided, so not informing the user about this error");
        }
    }

    @Override
    public boolean isValidInput() {
        // hide error as we are re-validating the input
        mErrorView.setVisibility(View.GONE);

        // check if this is required field and has an input value
        if (mField.isRequired() && !hasValue()) {
            String errorMessage = mField.getErrorMessage().getRequired();
            if(errorMessage==null || errorMessage.isEmpty()){
                errorMessage = getView().getResources().getString(com.nile.kmooc.R.string.error_select_field,
                        mField.getLabel());
            }
            handleError(errorMessage);
            return false;
        }

        //For select we should not have length checks as there is no input

        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        mInputView.setEnabled(enabled);
    }

    @Override
    public void setActionListener(IActionListener actionListener) {
        // no actions for this field
    }
}
